package com.datasift.yaml.circe

import cats.data.Xor
import io.circe.{Json, ParsingFailure, Parser}
import com.datasift.yaml.{Parser => YParser, CirceFacade}

class YawnParser extends Parser {
  implicit val facade = CirceFacade

  override def parse(input: String): Xor[ParsingFailure, Json] = {
    Xor.fromTry(YParser.parseFromString(input)).leftMap {
      t => ParsingFailure(t.getMessage, t)
    }
  }
}
