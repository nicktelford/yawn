package com.datasift.yawn

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

