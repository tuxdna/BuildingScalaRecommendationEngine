name := "BuildingScalaRecommendationEngine"

// scalaVersion := "2.10.2"

scalaVersion := "2.11.5"

version :="1.0"

libraryDependencies += "org.scala-lang" % "scala-reflect" % scalaVersion.value

libraryDependencies += "org.scala-lang.modules" %% "scala-xml" % "1.0.2"

libraryDependencies += "org.apache.spark" %% "spark-mllib" % "1.3.0"

libraryDependencies += "org.apache.spark" %% "spark-sql" % "1.3.0"

libraryDependencies += "org.apache.spark" %% "spark-catalyst" % "1.3.0"

libraryDependencies += "org.apache.spark" %% "spark-streaming" % "1.3.0"

libraryDependencies += "org.mongodb" %% "casbah" % "2.8.1"

libraryDependencies += "org.apache.kafka" % "kafka_2.11" % "0.8.2.0"

libraryDependencies += "org.apache.lucene" % "lucene-core" % "3.0.1"


