package com.datasift.yaml

import io.circe.{JsonNumber, Json}

import scala.collection.mutable

trait Facade[A] {

  def mappingContext: FContext[A]
  def mappingContext(anchor: String): FContext[A]
  def sequenceContext: FContext[A]
  def sequenceContext(anchor: String): FContext[A]

  def jnull: A
  def jfalse: A
  def jtrue: A
  def jnum(v: String): A
  def jint(v: String): A
  def jstring(v: String): A
}

trait FContext[A] {
  def anchor: Option[String]
  def add(v: A): Unit
  def result: A
}

object CirceFacade extends CirceFacade
class CirceFacade extends Facade[Json] {

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

  override val jnull: Json = Json.empty
  override val jfalse: Json = Json.False
  override val jtrue: Json = Json.True
  override def jnum(v: String): Json =
    if (v.startsWith("-.")) Json.Empty
    else Json.fromJsonNumber(JsonNumber.unsafeDecimal(v))
  override def jint(v: String): Json =
    Json.fromJsonNumber(JsonNumber.unsafeIntegral(v))
  override def jstring(v: String): Json = Json.string(v)
}
