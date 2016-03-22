package com.datasift.circe.yaml

import io.circe.{ParsingFailure, Parser}
import org.scalatest.FlatSpec
import io.circe.jawn.JawnParser

import scala.io.Source

class YamlParserSpec extends FlatSpec {

  def read(name: String) = {
    Source.fromInputStream(getClass.getResourceAsStream(name)).mkString
  }

  val yaml = new YamlParser()
  val json = new JawnParser()

  def inspect(parser: Parser, format: String, name: String) = {
    val path = s"/$format/$name.$format"
    println(s"Reading: $path")
    parser.parse(read(path)).recover {
      case ParsingFailure(msg, t) => println(msg); throw t
    }.map {
      ast => println(s"Parsed:\n${ast.toString}"); ast
    }
  }

  Seq("list", "mappings", "anchors", "complex").foreach { x =>
    x should "parse to circe AST" in {
      assert(inspect(yaml, "yaml", x) === inspect(json, "json", x))
    }
  }
}
