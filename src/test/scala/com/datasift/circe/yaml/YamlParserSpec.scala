package com.datasift.circe.yaml

import io.circe.Parser
import org.scalatest.FlatSpec
import io.circe.jawn.JawnParser

import scala.io.Source

class YamlParserSpec extends FlatSpec {

  def read(name: String) =
    Source.fromInputStream(getClass.getResourceAsStream(name)).mkString

  val yaml = new YamlParser()
  val json = new JawnParser()

  def parse(parser: Parser, format: String, name: String) =
    parser.parse(read(s"/$format/$name.$format")).recover { case x => throw x }

  Seq("list", "mappings", "anchors", "complex").foreach { name =>
    name should "parse to circe AST" in {
      assert(parse(yaml, "yaml", name) === parse(json, "json", name))
    }
  }
}
