package com.datasift.circe.yaml

import cats.data.Xor
import io.circe.{JsonNumber, Json, ParsingFailure, Parser}
import org.yaml.snakeyaml.nodes.{NodeId, Tag}
import org.yaml.snakeyaml.parser.ParserImpl
import org.yaml.snakeyaml.events._
import org.yaml.snakeyaml.reader.StreamReader
import org.yaml.snakeyaml.resolver.Resolver

import scala.annotation.tailrec
import scala.util.control.{NoStackTrace, NonFatal}
import scala.collection.mutable

// todo: write our own Mark => String function for error messages
sealed abstract class YamlParseError(msg: String)
  extends Exception(msg) //with NoStackTrace

case class UnexpectedInputError(event: Event, input: Option[Json] = None)
  extends YamlParseError(
    "Unexpected input" +
      input.map(": " + _.toString).getOrElse(event.toString) +
      s"\n${event.getStartMark.toString}")

case class AnchorNotFoundError(anchor: String, event: Event)
  extends YamlParseError(
    s"Anchor '$anchor' not found: ${event.getStartMark.toString}")

case class InvalidKeyError(node: Json)
  extends YamlParseError(s"$node is not a valid mapping key")

sealed trait ParserContext {
  def anchor: Option[String]
  def add(value: Json, sourceEvent: Event, ctx: List[ParserContext] = Nil): ParserContext
}

case class SequenceContext(elements: mutable.Buffer[Json],
                           anchor: Option[String])
  extends ParserContext {

  final def add(value: Json, sourceEvent: Event, ctx: List[ParserContext] = Nil): ParserContext =
    SequenceContext(elements += value, anchor)
}

case class MappingKeyContext(elements: mutable.Buffer[(String, Json)],
                             anchor: Option[String])
  extends ParserContext {

  final def add(value: Json, sourceEvent: Event, ctx: List[ParserContext] = Nil): ParserContext =
    MappingValueContext(
      value.asString.getOrElse(throw InvalidKeyError(value)), elements, anchor)
}

case class MappingValueContext(key: String,
                               elements: mutable.Buffer[(String, Json)],
                               anchor: Option[String])
  extends ParserContext {

  final def add(value: Json, sourceEvent: Event, ctx: List[ParserContext] = Nil): ParserContext =
    MappingKeyContext(elements += key -> value, anchor)
}

class YamlParser extends Parser {
  val resolver = new Resolver

  override def parse(input: String): Xor[ParsingFailure, Json] = try {
    val parser = new ParserImpl(new StreamReader(input))
    val iterator = new Iterator[Event] {
      override def hasNext: Boolean = parser.peekEvent() != null
      override def next(): Event = parser.getEvent
    }
    Xor.right { parse(iterator) }
  } catch {
    case p: ParsingFailure => Xor.left(p)
    case NonFatal(t) =>
      Xor.left(ParsingFailure("Failed to parse YAML from stream", t))
  }

  @tailrec
  private[yaml] final def parse(stream: Iterator[Event],
                                ctx: List[ParserContext] = Nil,
                                aliases: Map[String, Json] = Map.empty): Json = {
    val event = stream.next()

    val (value, newCtx, newAliases) = event match {
      case e: StreamStartEvent => (None, ctx, aliases)
      case e: StreamEndEvent => ctx match {
        case Nil => (None, ctx, aliases)
        case _ => throw UnexpectedInputError(e)
      }
      // todo: support multiple docs in a stream
      case e: DocumentStartEvent => (None, ctx, aliases)
      case e: DocumentEndEvent => (None, ctx, aliases)
      case e: MappingStartEvent =>
        (None, MappingKeyContext(mutable.Buffer.empty, Option(e.getAnchor)) :: ctx, aliases)
      case e: MappingEndEvent => ctx match {
        case MappingKeyContext(kvs, None) :: stack =>
          (Some(Json.fromFields(kvs)), stack, aliases)
        case MappingKeyContext(kvs, Some(anchor)) :: stack =>
          val node = Json.fromFields(kvs)
          (Some(node), stack, aliases + (anchor -> node))
        case _ => throw UnexpectedInputError(e)
      }
      case e: SequenceStartEvent =>
        (None, SequenceContext(mutable.Buffer.empty, Option(e.getAnchor)) :: ctx, aliases)
      case e: SequenceEndEvent => ctx match {
        case SequenceContext(elems, None) :: stack =>
          (Some(Json.fromValues(elems)), stack, aliases)
        case SequenceContext(elems, Some(anchor)) :: stack =>
          val node = Json.fromValues(elems)
          (Some(node), stack, aliases + (anchor -> node))
        case _ => throw UnexpectedInputError(e)
      }
      case e: ScalarEvent =>
        val value = e.getValue
        val anchor = e.getAnchor
        val tag = Option(e.getTag)
          .map(new Tag(_))
          .getOrElse {
            resolver.resolve(
              NodeId.scalar, value, e.getImplicit.canOmitTagInPlainScalar)
          }
        val node = (value, tag) match {
          case (null, _) => Json.empty
          case (v, Tag.BOOL) => Deserializer
            .boolean(v)
            .map(Json.bool)
            .getOrElse(Json.string(v))
          case (v, Tag.FLOAT | Tag.INT) => JsonNumber
            .fromString(v)
            .map(Json.fromJsonNumber)
            .getOrElse(Json.string(v))
          case (v, Tag.NULL) => Json.empty
          case (v, _) => Json.string(v)
        }

        // add aliases if they're defined
        node match {
          case node if anchor == null => (Some(node), ctx, aliases)
          case node => (Some(node), ctx, aliases + (anchor -> node))
        }

      case e: AliasEvent =>
        val anchor = e.getAnchor
        val node = aliases.getOrElse(
          anchor, throw AnchorNotFoundError(anchor, e))
        (Some(node), ctx, aliases)
    }

    value match {
      case Some(v) if newCtx.isEmpty => v
      case value =>
        val context = value.map { v =>
          val head :: tail = newCtx
          head.add(v, event, newCtx) :: tail
        }.getOrElse(newCtx)

        parse(stream, context, newAliases)
    }
  }
}

private[yaml] object Deserializer {
  private val booleans = Map(
    "yes" -> true, "true" -> true, "on" -> true,
    "no" -> false, "false" -> false, "off" -> false)

  def boolean(value: String): Option[Boolean] = booleans.get(value.toLowerCase)
}
