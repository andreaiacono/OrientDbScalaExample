name := "OrientDbScalaExample"

version := "1.0"

scalaVersion := "2.11.8"

libraryDependencies ++= Seq(
  "com.orientechnologies" % "orientdb-core" % "2.2.0",
  "com.orientechnologies" % "orientdb-client" % "2.2.0",
  "com.orientechnologies" % "orientdb-jdbc" % "2.2.0",
  "com.orientechnologies" % "orientdb-graphdb" % "2.2.0",
  "com.orientechnologies" % "orientdb-distributed" % "2.2.0",
  "com.tinkerpop.blueprints" % "blueprints-core" % "2.6.0"
)