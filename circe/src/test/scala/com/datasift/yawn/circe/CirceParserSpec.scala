package com.datasift.yawn.circe

import com.datasift.yawn.ParserSpec
import io.circe.jawn.JawnParser
import io.circe.{Parser => CirceParser, Json}

class CirceParserSpec extends ParserSpec[CirceParser, Json] {

  override val yaml = new YawnParser()
  override val json = new JawnParser()

  override def parse(parser: CirceParser): (String) => Json =
    parser.parse(_).fold(e => throw new Exception(e.getMessage), identity)
}
