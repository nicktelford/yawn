package com.datasift.yawn.circe

import com.datasift.yawn.{SupportParser, FContext, Facade}
import io.circe.{JsonNumber, Json}

import scala.collection.mutable

object Parser extends SupportParser[Json] {

  override implicit def facade: Facade[Json] = new Facade[Json] {

    override def mappingContext: FContext[Json] = new FContext[Json] {
      private var key: String = null
      private val kvs: mutable.Buffer[(String, Json)] = mutable.Buffer.empty
      override def anchor: Option[String] = None
      override def add(v: Json) {
        if (key == null) key = v.asString.getOrElse(v.toString)
        else { kvs += (key -> v); key = null }
      }
      override def result: Json = Json.fromFields(kvs)
    }

    override def mappingContext(a: String): FContext[Json] = new FContext[Json] {
      private var key: String = null
      private val kvs: mutable.Buffer[(String, Json)] = mutable.Buffer.empty
      override def anchor: Option[String] = Option(a)
      override def add(v: Json) {
        if (key == null) key = v.asString.getOrElse(v.toString)
        else { kvs += (key -> v); key = null }
      }
      override def result: Json = Json.fromFields(kvs)
    }

    override def sequenceContext(a: String): FContext[Json] = new FContext[Json] {
      private val kvs: mutable.Buffer[Json] = mutable.Buffer.empty
      override def anchor: Option[String] = Option(a)
      override def add(v: Json): Unit = kvs += v
      override def result: Json = Json.fromValues(kvs)
    }

    override def sequenceContext: FContext[Json] = new FContext[Json] {
      private val kvs: mutable.Buffer[Json] = mutable.Buffer.empty
      override def anchor: Option[String] = None
      override def add(v: Json): Unit = kvs += v
      override def result: Json = Json.fromValues(kvs)
    }

    override val jnull: Json = Json.Null
    override val jfalse: Json = Json.False
    override val jtrue: Json = Json.True
    override def jnum(v: String): Json =
      JsonNumber.fromString(v)
        .map(Json.fromJsonNumber)
        .getOrElse(throw new NumberFormatException(v))
    override def jint(v: String): Json = Json.fromInt(v.toInt)
    override def jstring(v: String): Json = Json.fromString(v)
  }
}
