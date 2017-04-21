lazy val buildSettings = Seq(
  organization := "com.ithaca",
  scalaVersion := "2.12.1",
  name := "fs2-reactive",
  version := "0.1.0-SNAPSHOT"
)

lazy val commonScalacOptions = Seq(
  "-encoding",
  "UTF-8",
  "-language:existentials",
  "-language:higherKinds",
  "-language:implicitConversions",
  "-language:experimental.macros",
  "-language:postfixOps"
)

lazy val commonResolvers = Seq(
 Resolver.sonatypeRepo("releases")
)

lazy val coverageSettings = Seq(
  coverageMinimum := 60,
  coverageFailOnMinimum := false
)

lazy val commonSettings = Seq(
    resolvers := commonResolvers,
  scalacOptions ++= commonScalacOptions,
  libraryDependencies ++= Seq(
    "co.fs2" %% "fs2-core" % "0.9.4",
    "org.reactivestreams" % "reactive-streams" % "1.0.0",
    "org.scalatest" %% "scalatest" % "3.0.1" % "test",
    "org.scalacheck" %% "scalacheck" % "1.13.4" % "test",
    "org.reactivestreams" % "reactive-streams-tck" % "1.0.0" % "test"
  )
) ++ coverageSettings ++ buildSettings

lazy val docSettings = tutSettings ++ Seq(
  libraryDependencies ++= Seq(
    "com.typesafe.akka" %% "akka-stream" % "2.5.0"
  ),
  tutTargetDirectory := (baseDirectory in ThisBuild).value
)

lazy val core = (project in file("core"))
  .settings(moduleName := "core")
  .settings(commonSettings)

lazy val docs = (project in file("docs"))
  .settings(moduleName := "docs")
  .dependsOn(core)
  .settings(commonSettings)
  .settings(docSettings)

lazy val root = (project in file("."))
  .aggregate(core, docs)
