package com.datasift.yawn

import org.scalatest.FunSpec

import scala.io.Source

trait ParserSpec[P, A] extends FunSpec {

  def yaml: P
  def json: P
  def parse(parser: P): String => A

  def read(name: String) =
    Source.fromInputStream(getClass.getResourceAsStream(name)).mkString

  def file(format: String, name: String): String =
    s"/$format/$name.$format"

  describe("yaml parser") {
    Seq("list", "mappings", "anchors", "complex").foreach { filename =>
      it(s"should parse $filename.yaml as $filename.json") {
        assert(
          parse(yaml)(read(file("yaml", filename))) ===
          parse(json)(read(file("json", filename)))
        )
      }
    }
  }
}
