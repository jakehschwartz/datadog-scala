name := "datadog-scala"

organization := "com.jakehschwartz"

scalaVersion := "2.12.6"

scalacOptions ++= Seq(
  "-unchecked",
  "-deprecation",
  "-feature",
  "-language:existentials",
  "-language:implicitConversions"
)

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-stream" % "2.5.14",
  "com.typesafe.akka" %% "akka-http"   % "10.1.3",
  "org.json4s" %% "json4s-native" % "3.6.0",
  "org.json4s" %% "json4s-jackson" % "3.6.0",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.9.0",
  "org.specs2" %% "specs2-core" % "4.3.2" % "test",
  "org.slf4j" % "slf4j-simple" % "1.7.25" % "test"
)

pomIncludeRepository := { _ => false }

publishMavenStyle := true

publishTo := {
  val nexus = "https://oss.sonatype.org/"
  if (isSnapshot.value)
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else
    Some("releases" at nexus + "service/local/staging/deploy/maven2")
}

// License of your choice
licenses := Seq("MIT" -> url("https://opensource.org/licenses/MIT"))
homepage := Some(url("https://github.com/jakehschwartz/datadog-scala"))
scmInfo := Some(
  ScmInfo(
    browseUrl = url("https://github.com/jakehschwartz/datadog-scala"),
    connection = "https://github.com/jakehschwartz/datadog-scala.git"
  )
)
developers := List(
  Developer(id = "jakehschwartz", name = "Jake Schwartz", email = "jakehschwartz@gmail.com", url = url("https://www.jakehschwartz.com"))
)