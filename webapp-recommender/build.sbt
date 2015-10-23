name := "webapp-recommender"

version := "1.0-SNAPSHOT"

scalaVersion := "2.10.4"

scalaVersion in ThisBuild := "2.10.4"

val sparkVersion = "1.3.0"

val akkaVersion = "2.3.6"

libraryDependencies ++= Seq(
  jdbc,
  anorm,
  cache,
  // "org.scala-lang" % "scala-xml" % "2.11.0-M4",
  // "org.apache.spark" %% "spark-core" % sparkVersion,         // Spark
  // "org.apache.spark" %% "spark-mllib" % sparkVersion,        // Spark MLLIB
  // "com.typesafe.akka" %% "akka-actor" % akkaVersion,         // Akka Actor
  // "com.typesafe.akka" %% "akka-slf4j" % akkaVersion,         // Akka SLF4J
  "org.webjars" %% "webjars-play" % "2.2.0",
  "org.webjars" % "bootstrap" % "3.1.1",
  "org.webjars" % "html5shiv" % "3.7.0",
  "org.webjars" % "respond" % "1.4.2",
  "com.twitter" %% "algebird-core" % "0.9.0",                 // Twitter Algebird
  "net.databinder.dispatch" %% "dispatch-core" % "0.11.1",   // HTTP client
  "org.reactivemongo" %% "reactivemongo" % "0.10.5.0.akka22", // MongoDB Reactive
  "org.mongodb" %% "casbah" % "2.8.1",                       // MongoDB Casbah
  "com.sksamuel.elastic4s" %% "elastic4s" % "1.4.14"
)

play.Project.playScalaSettings

