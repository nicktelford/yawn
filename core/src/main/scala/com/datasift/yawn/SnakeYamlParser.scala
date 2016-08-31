package com.datasift.yawn

import org.yaml.snakeyaml.error.Mark
import org.yaml.snakeyaml.events._
import org.yaml.snakeyaml.nodes.{NodeId, Tag}
import org.yaml.snakeyaml.reader.StreamReader
import org.yaml.snakeyaml.resolver.Resolver

import scala.annotation.tailrec
import scala.util.control.NoStackTrace

sealed abstract class YamlParseError(msg: String, mark: Mark)
  extends Exception(msg + mark.toString) with NoStackTrace

object UnexpectedInputError {
  private def format(event: Event): String = {
    val tpe   = event.getClass.getSimpleName
    val value = event match {
      case e: ScalarEvent =>
        val style = Option(e.getStyle).map(_.toString).getOrElse("")
        s"($style${e.getValue}$style)"
      case _ => ""
    }
    tpe + value + event.getStartMark.toString
  }
}
case class UnexpectedInputError(event: Event, mark: Mark) extends YamlParseError(
  s"Unexpected input: ${UnexpectedInputError.format(event)}", mark)

case class UnexpectedEndOfInputError(mark: Mark)
  extends YamlParseError(s"Unexpected end of input", mark)

case class AnchorNotFoundError(anchor: String, event: Event, mark: Mark)
  extends YamlParseError(
    s"Anchor '$anchor' not found: ${event.getStartMark}", mark)

class SnakeYamlParser[A](reader: StreamReader)(implicit facade: Facade[A]) {

  private val resolver = new Resolver

  @tailrec
  final def parse(stream: Iterator[Event],
                  ctx: List[FContext[A]] = Nil,
                  aliases: Map[String, A] = Map()): A = {
    val event = if (stream.hasNext)
      stream.next()
    else
      throw UnexpectedEndOfInputError(reader.getMark)

    val (value, newCtx, newAliases) = event match {
      case e: StreamStartEvent => (None, ctx, aliases)
      case e: StreamEndEvent => ctx match {
        case Nil => (None, ctx, aliases)
        case _ => throw UnexpectedInputError(e, reader.getMark)
      }
      // todo: support multiple docs in a stream
      case e: DocumentStartEvent => (None, ctx, aliases)
      case e: DocumentEndEvent => (None, ctx, aliases)
      case e: MappingStartEvent =>
        (None, facade.mappingContext(e.getAnchor) :: ctx, aliases)
      case e: MappingEndEvent => ctx match {
        case c :: stack =>
          val mapping = c.result
          (Some(mapping), stack, c.anchor.map(a => aliases + (a -> mapping)).getOrElse(aliases))
        case _ => throw UnexpectedInputError(e, reader.getMark)
      }
      case e: SequenceStartEvent =>
        (None, facade.sequenceContext(e.getAnchor) :: ctx, aliases)
      case e: SequenceEndEvent => ctx match {
        case c :: stack =>
          val sequence = c.result
          (Some(sequence), stack, c.anchor.map(a => aliases + (a -> sequence)).getOrElse(aliases))
        case _ => throw UnexpectedInputError(e, reader.getMark)
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

        val node = try {
          (value, tag) match {
            case (null, _) => facade.jnull
            case (v, Tag.BOOL) => boolean(v).getOrElse(facade.jstring(v))
            case (v, Tag.FLOAT) => facade.jnum(v)
            case (v, Tag.INT) => facade.jint(v)
            case (v, Tag.NULL) => facade.jnull
            case (v, _) => facade.jstring(v)
          }
        } catch {
          case ex: NumberFormatException =>
            throw UnexpectedInputError(e, reader.getMark)
        }

        // add aliases if they're defined
        node match {
          case node if anchor == null => (Some(node), ctx, aliases)
          case node => (Some(node), ctx, aliases + (anchor -> node))
        }

      case e: AliasEvent =>
        val anchor = e.getAnchor
        val node = aliases.getOrElse(
          anchor, throw AnchorNotFoundError(anchor, e, reader.getMark))
        (Some(node), ctx, aliases)
    }

    (value, newCtx) match {
      case (Some(v), Nil) => v
      case (v, context) =>
        v.foreach(v => context.headOption.foreach(_.add(v)))
        parse(stream, context, newAliases)
    }
  }

  private val booleans = Map(
    "yes" -> facade.jtrue, "true" -> facade.jtrue, "on" -> facade.jtrue,
    "no" -> facade.jfalse, "false" -> facade.jfalse, "off" -> facade.jfalse)

  def boolean(value: String): Option[A] = booleans.get(value.toLowerCase)
}
