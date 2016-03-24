package com.datasift.yawn.circe

import com.datasift.yawn.ParserSpec
import io.circe.{Parser => CirceParser, Json}
import io.circe.jawn.JawnParser

class CirceParserSpec extends ParserSpec[CirceParser, Json] {

  val yaml = new YawnParser()
  val json = new JawnParser()

  override def parse(parser: CirceParser): (String) => Json =
    parser.parse(_).fold(e => throw new Exception(e.getMessage), identity)
}
