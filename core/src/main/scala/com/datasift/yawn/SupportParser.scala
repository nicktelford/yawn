package com.datasift.yawn

import java.io.{File, FileReader, Reader}
import java.nio.ByteBuffer
import java.nio.channels.ReadableByteChannel

import org.yaml.snakeyaml.events.Event
import org.yaml.snakeyaml.parser.ParserImpl
import org.yaml.snakeyaml.reader.StreamReader

import scala.util.Try

trait SupportParser[A] {
  implicit def facade: Facade[A]

  def parseUnsafeString(string: String): A =
    parseFromParserUnsafe(new StreamReader(string))

  def parseUnsafeReader(reader: Reader): A =
    parseFromParserUnsafe(new StreamReader(reader))

  def parseFromString(s: String): Try[A] =
    Try(parseUnsafeString(s))

  def parseFromPath(path: String): Try[A] =
    Try(parseUnsafeReader(new FileReader(path)))

  def parseFromFile(file: File): Try[A] =
    Try(parseUnsafeReader(new FileReader(file)))

  def parseFromChannel(channel: ReadableByteChannel): Try[A] = ???
  def parseFromByteBuffer(buffer: ByteBuffer): Try[A] = ???

  protected def parseFromParserUnsafe(reader: StreamReader): A = {
    val parser = new ParserImpl(reader)
    val iterator = new Iterator[Event] {
      override def hasNext: Boolean = parser.peekEvent() != null
      override def next(): Event = parser.getEvent
    }
    new SnakeYamlParser().parse(iterator)
  }
}
