val circeVersion = "0.3.0"
val snakeYamlVersion = "1.17"
val scalaTestVersion = "3.0.0-M15"
val javaRuntimeVersion = "1.8"

name := "circe-yaml"
description := "Parses YAML streams in to the circe AST"
organization := "com.datasift.circe"
organizationName := "DataSift Inc."
version := "1.0.0"
scalaVersion := "2.11.8"
scalacOptions += s"-target:jvm-$javaRuntimeVersion"
javacOptions ++= Seq("-target", javaRuntimeVersion)

libraryDependencies ++= Seq(
  "io.circe" %% "circe-core" % circeVersion,
  "org.yaml" % "snakeyaml" % snakeYamlVersion,

  "org.scalatest" %% "scalatest" % scalaTestVersion % Test,
  "io.circe" %% "circe-jawn" % circeVersion % Test
)

