package chapter07

import java.io._

import org.apache.commons.io.FileUtils
import org.apache.spark.mllib.recommendation.{ALS, MatrixFactorizationModel, Rating}
import org.apache.spark.{SparkConf, SparkContext}

object SaveIdMappings {
  def removePathIfExists(path: String): Unit = {
    if (new File(path).exists()) {
      FileUtils.deleteDirectory(new File(path))
    }
  }

  def main(args: Array[String]): Unit = {
    val processors = Runtime.getRuntime.availableProcessors()
    val conf = new SparkConf(false)
      .setMaster(s"local[${processors}]")
      .setAppName(SaveIdMappings.getClass.getName)

    conf.set("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
    conf.set("spark.executor.memory", "2g")

    val sc = new SparkContext(conf)

    val fileName = "datasets/amazon-ratings.csv"

    val rawData = sc.textFile(fileName, 100).
      map(_.split("\t")).
      flatMap { arr =>
      if (arr.length == 3) {
        val Array(c, a, r) = arr
        if (r.equals("rating")) None
        else Some((c, a, r.toDouble))
      } else None
    }

    val customerToIdRDD = rawData.map(_._1).distinct().zipWithUniqueId()
    val asinToIdRDD = rawData.map(_._2).distinct().zipWithUniqueId()
    val custFile = "mappings/customerToId"
    val asinFile = "mappings/asinToId"
    removePathIfExists(custFile)
    removePathIfExists(asinFile)
    customerToIdRDD.map { case (x, y) => s"$x,$y" }.saveAsTextFile(custFile)
    asinToIdRDD.map { case (x, y) => s"$x,$y" }.saveAsTextFile(asinFile)
  }

}
