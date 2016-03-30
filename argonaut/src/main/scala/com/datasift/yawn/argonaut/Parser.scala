package com.datasift.yawn.argonaut

import argonaut.{JsonNumber, Json}
import com.datasift.yawn.{SupportParser, FContext, Facade}

import scala.collection.mutable

object Parser extends SupportParser[Json] {
  override implicit def facade: Facade[Json] = new Facade[Json] {
    override def mappingContext: FContext[Json] = new FContext[Json] {
      private var key: String = null
      private val kvs: mutable.Buffer[(String, Json)] = mutable.Buffer.empty
      override def anchor: Option[String] = None
      override def add(v: Json) {
        if (key == null) key = v.string.getOrElse(v.toString)
        else { kvs += (key -> v); key = null }
      }
      override def result: Json = Json(kvs: _*)
    }

    override def mappingContext(a: String): FContext[Json] = new FContext[Json] {
      private var key: String = null
      private val kvs: mutable.Buffer[(String, Json)] = mutable.Buffer.empty
      override def anchor: Option[String] = Option(a)
      override def add(v: Json) {
        if (key == null) key = v.string.getOrElse(v.toString)
        else { kvs += (key -> v); key = null }
      }
      override def result: Json = Json(kvs: _*)
    }

    override def sequenceContext(a: String): FContext[Json] = new FContext[Json] {
      private val kvs: mutable.Buffer[Json] = mutable.Buffer.empty
      override def anchor: Option[String] = Option(a)
      override def add(v: Json): Unit = kvs += v
      override def result: Json = Json.array(kvs: _*)
    }

    override def sequenceContext: FContext[Json] = new FContext[Json] {
      private val kvs: mutable.Buffer[Json] = mutable.Buffer.empty
      override def anchor: Option[String] = None
      override def add(v: Json): Unit = kvs += v
      override def result: Json = Json.array(kvs: _*)
    }

    override def jnull: Json = Json.jNull
    override def jtrue: Json = Json.jTrue
    override def jfalse: Json = Json.jFalse
    override def jnum(v: String): Json =
      Json.jNumber(JsonNumber.unsafeDecimal(v))
    override def jint(v: String): Json =
      Json.jNumber(JsonNumber.unsafeDecimal(v))
    override def jstring(v: String): Json = Json.jString(v)
  }
}
