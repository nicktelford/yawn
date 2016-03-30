package com.datasift.yawn.argonaut

import argonaut.{Json, Parse}

import scalaz.\/

object YamlParse extends Parse[String] {

  override def parse(value: String): String \/ Json =
    \/.fromTryCatchNonFatal(Parser.parseUnsafeString(value))
      .leftMap(_.getMessage)
}

