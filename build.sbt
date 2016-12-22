name := "trucking-schema-registry"

version := "0.1"

organization := "com.hortonworks.orendainx"

scalaVersion := "2.11.8"

resolvers += Resolver.mavenLocal

libraryDependencies ++= Seq(
  "com.hortonworks.registries" % "schema-registry-serdes" % "0.1.0-SNAPSHOT",

  "com.typesafe.scala-logging" %% "scala-logging" % "3.5.0",
  "ch.qos.logback" % "logback-classic" % "1.1.7",

  "com.typesafe" % "config" % "1.3.1"
)
