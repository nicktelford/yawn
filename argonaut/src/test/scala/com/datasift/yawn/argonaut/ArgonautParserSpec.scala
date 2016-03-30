package com.datasift.yawn.argonaut

import argonaut.{Json, Parse}
import com.datasift.yawn.ParserSpec

class ArgonautParserSpec extends ParserSpec[Parse[String], Json] {

  override val yaml = YamlParse
  override val json = Parse

  override def parse(parser: Parse[String]): (String) => Json = {
    value: String =>
      parser.parse(value).valueOr(e => throw new Exception(e))
    }
}
