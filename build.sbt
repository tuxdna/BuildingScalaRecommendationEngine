name := "BuildingScalaRecommendationEngine"

scalaVersion := "2.10.4"

// val sprayVersion = "1.3.3"


// scalaVersion := "2.11.5"

javaOptions in(Test, run) += "-XX:+UseConcMarkSweepGC"

version := "1.0"

ivyScala := ivyScala.value map {
  _.copy(overrideScalaVersion = true)
}

// libraryDependencies += "org.scala-lang" % "scala-reflect" % scalaVersion.value

libraryDependencies += "org.scala-lang" % "scala-reflect" % "2.10.4"

// libraryDependencies += "org.scala-lang.modules" %% "scala-xml" % "1.0.2"

libraryDependencies += "org.apache.spark" %% "spark-mllib" % "1.3.0"

libraryDependencies += "org.apache.spark" %% "spark-sql" % "1.3.0"

libraryDependencies += "org.apache.spark" %% "spark-catalyst" % "1.3.0"

libraryDependencies += "org.apache.spark" %% "spark-streaming" % "1.3.0"

libraryDependencies += "org.mongodb" %% "casbah" % "2.8.1"

libraryDependencies += "org.apache.kafka" %% "kafka" % "0.8.2.0"

libraryDependencies += "org.apache.lucene" % "lucene-core" % "3.6.2"

libraryDependencies += "org.apache.lucene" % "lucene-analyzers" % "3.6.2"

libraryDependencies += "org.apache.lucene" % "lucene-spellchecker" % "3.6.2"

libraryDependencies += "org.reactivemongo" %% "reactivemongo" % "0.10.5.0.akka23"

libraryDependencies += "com.typesafe.play" %% "play-json" % "2.3.9"

resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"


libraryDependencies += "org.mongodb.mongo-hadoop" % "mongo-hadoop-core" % "1.4.0"

libraryDependencies += "org.apache.hadoop" % "hadoop-client" % "2.2.0"

libraryDependencies += "org.mongodb" % "mongo-java-driver" % "2.11.4"

libraryDependencies += "com.twitter" %% "algebird-core" % "0.9.0"

libraryDependencies += "com.databricks" %% "spark-csv" % "1.1.0"

libraryDependencies += "commons-io" % "commons-io" % "2.4"

libraryDependencies += "com.sksamuel.elastic4s" %% "elastic4s" % "1.4.14"

libraryDependencies += "io.spray" %% "spray-can" % "1.3.3"

libraryDependencies += "io.spray" %% "spray-routing" % "1.3.3"

libraryDependencies += "io.spray" %% "spray-json" % "1.3.2"

// val akkaV = "2.3.9"
// "io.spray"            %%  "spray-testkit" % sprayVersion  % "test",
// "com.typesafe.akka"   %%  "akka-actor"    % akkaV,
// "com.typesafe.akka"   %%  "akka-testkit"  % akkaV   % "test",
// "org.specs2"          %%  "specs2-core"   % "2.3.7" % "test"


retrieveManaged := true


