name := "trucking-schema-registry"

version := "0.3.1"

organization := "com.hortonworks.orendainx"

scalaVersion := "2.11.8"

resolvers += Resolver.mavenLocal
//resolvers := Seq("Hortonworks Nexus" at "http://nexus-private.hortonworks.com/nexus/content/groups/public")

libraryDependencies ++= Seq(
  "com.hortonworks.registries" % "schema-registry-serdes" % "0.1.0-SNAPSHOT",
  //"com.hortonworks.registries" % "schema-registry-common" % "0.0.1.2.1.1.0-7",

  "com.typesafe" % "config" % "1.3.1",

  // Logging
  "com.typesafe.scala-logging" %% "scala-logging" % "3.5.0",
  "ch.qos.logback" % "logback-classic" % "1.1.7"
)
