package com.datasift.yawn

import org.scalatest.FlatSpec

import scala.io.Source

trait ParserSpec[P, A] extends FlatSpec {

  def yaml: P
  def json: P
  def parse(parser: P): String => A

  def read(name: String) =
    Source.fromInputStream(getClass.getResourceAsStream(name)).mkString

  def file(format: String, name: String): String =
    s"/$format/$name.$format"

  Seq("list", "mappings", "anchors", "complex").foreach { name =>
    name should "parse to circe AST" in {
      assert(
        parse(yaml)(read(file("yaml", name))) ===
        parse(json)(read(file("json", name)))
      )
    }
  }
}
