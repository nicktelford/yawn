val circeVersion = "0.5.0-M3"
val argonautVersion = "6.1"
val snakeYamlVersion = "1.17"
val scalaTestVersion = "3.0.0-M15"
val javaRuntimeVersion = "1.8"

lazy val yawnSettings = Seq(
  description := "Parses YAML streams in to the circe AST",
  organization := "com.datasift.yawn",
  organizationName := "DataSift Inc.",
  version := "1.0.0",
  scalaVersion := "2.11.8",
  scalacOptions += s"-target:jvm-$javaRuntimeVersion",
  javacOptions ++= Seq("-target", javaRuntimeVersion),

  libraryDependencies ++= Seq(
    "org.yaml" % "snakeyaml" % snakeYamlVersion,
    "org.scalatest" %% "scalatest" % scalaTestVersion % Test
  ),

  testOptions in Test += Tests.Argument("-oF")
)

lazy val noPublish = Seq(
  publish := {},
  publishLocal := {},
  publishArtifact := false
)

lazy val root = project.in(file("."))
  .aggregate(core, argonaut, circe)
  .settings(name := "yawn")
  .settings(yawnSettings: _*)
  .settings(noPublish: _*)

lazy val core = project.in(file("core"))
  .settings(name := "core")
  .settings(moduleName := "yawn-core")
  .settings(yawnSettings: _*)

lazy val argonaut = project.in(file("argonaut"))
  .dependsOn(core % "compile->compile;test->test")
  .settings(name := "argonaut")
  .settings(moduleName := "yawn-argonaut")
  .settings(yawnSettings: _*)
  .settings(libraryDependencies ++= Seq(
    "io.argonaut" %% "argonaut" % argonautVersion,
    "org.spire-math" %% "jawn-argonaut" % "0.8.4" % Test
  ))

lazy val circe = project.in(file("circe"))
  .dependsOn(core % "compile->compile;test->test")
  .settings(name := "circe")
  .settings(moduleName := "yawn-circe")
  .settings(yawnSettings: _*)
  .settings(libraryDependencies ++= Seq(
    "io.circe" %% "circe-core" % circeVersion,
    "io.circe" %% "circe-jawn" % circeVersion % Test
  ))

