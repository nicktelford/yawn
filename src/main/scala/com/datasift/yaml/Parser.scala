package com.datasift.yaml

import java.io.{File, FileReader, Reader}

import org.yaml.snakeyaml.events.Event
import org.yaml.snakeyaml.parser.ParserImpl
import org.yaml.snakeyaml.reader.StreamReader

import scala.util.Try

object Parser {

  def parseUnsafeString[A : Facade](string: String): A =
    parseFromParserUnsafe(new StreamReader(string))

  def parseUnsafeReader[A : Facade](reader: Reader): A =
    parseFromParserUnsafe(new StreamReader(reader))

  def parseFromString[A : Facade](s: String): Try[A] =
    Try(parseUnsafeString(s))

  def parseFromPath[A: Facade](path: String): Try[A] =
    Try(parseUnsafeReader(new FileReader(path)))

  def parseFromFile[A : Facade](file: File): Try[A] =
    Try(parseUnsafeReader(new FileReader(file)))

  protected def parseFromParserUnsafe[A : Facade](reader: StreamReader): A = {
    val parser = new ParserImpl(reader)
    val iterator = new Iterator[Event] {
      override def hasNext: Boolean = parser.peekEvent() != null
      override def next(): Event = parser.getEvent
    }
    new SnakeYamlParser().parse(iterator)
  }
}
