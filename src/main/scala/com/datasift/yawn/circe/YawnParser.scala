package com.datasift.yawn.circe

import java.io.File
import java.nio.ByteBuffer

import cats.data.Xor
import io.circe.{Json, ParsingFailure, Parser => CirceParser}

import scala.util.Try

class YawnParser extends CirceParser {
  private[this] final def fromTry(t: Try[Json]): Xor[ParsingFailure, Json] =
    Xor.fromTry(t).leftMap(error => ParsingFailure(error.getMessage, error))

  final def parse(input: String): Xor[ParsingFailure, Json] =
    fromTry(Parser.parseFromString(input))

  final def parseFile(file: File): Xor[ParsingFailure, Json] =
    fromTry(Parser.parseFromFile(file))

  final def parseByteBuffer(buffer: ByteBuffer): Xor[ParsingFailure, Json] =
    fromTry(Parser.parseFromByteBuffer(buffer))
}
