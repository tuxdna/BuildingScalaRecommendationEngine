package chapter06

import scala.Array.canBuildFrom
import scala.reflect.runtime.universe
import scala.reflect.runtime.universe.TypeTag.Int

import org.apache.spark.SparkConf
import org.apache.spark.SparkContext
import org.apache.spark.mllib.clustering.KMeans
import org.apache.spark.mllib.feature.HashingTF
import org.apache.spark.mllib.linalg.SparseVector
import org.apache.spark.mllib.linalg.Vector
import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.SQLContext
import org.apache.spark.sql.functions.col
import org.apache.spark.sql.functions.udf

object ContentBasedRSExample {

  val colSep = "\t"
  val catSep = "::"
  val titleToTerms = (title: String) => title.toLowerCase().replaceAll("""[^\w\s]+""", "").split(" ")

  val dim = math.pow(2, 10).toInt
  val hashingTF = new HashingTF(dim)

  val featureVectorsUDF = udf[Vector, String, String, String, String, String] {
    (title, group, salesrank, averageRating, categories) =>
      val cats = categories.split("::").map(_.trim).filter(_.length > 0)
      val row = (title, group, cats.toList)

      val titleTerms = titleToTerms(title)
      val allTerms = titleTerms ++ Array(group) ++ cats
      val t1 = allTerms.toSeq
      val vector1 = hashingTF.transform(t1)
      var sr = 0.0
      var ar = 0.0
      try { sr = salesrank.toDouble } catch { case _: Exception => }
      try { ar = averageRating.toDouble } catch { case _: Exception => }
      val aVec = vector1.asInstanceOf[SparseVector]
      val vector2 = Vectors.sparse(dim + 2,
        aVec.indices ++ Array(dim, dim + 1),
        aVec.values ++ Array(sr, ar))
      vector2
  }

  def main(args: Array[String]): Unit = {
    val conf = new SparkConf(false).
      setMaster("local[2]").
      setAppName("ContentBasedRSExample").
      set("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
    val sc = new SparkContext(conf)
    val sqlContext = new SQLContext(sc)

    val fileName = "datasets/content-based-dataset-new.csv"
    val df = sqlContext.load("com.databricks.spark.csv",
      Map("path" -> fileName,
        "header" -> "true", "delimiter" -> colSep,
        "quote" -> "\0"))
    df.printSchema()
    val df2 = df.withColumn("features",
      featureVectorsUDF(
        df("title"),
        df("group"),
        df("salesrank"),
        df("averageRating"),
        df("categories")))
    df2.printSchema()
    val itemsRDD: RDD[Vector] = df2.select("features").rdd.
      map(x => x(0).asInstanceOf[Vector])

    val finalRDD = itemsRDD
    finalRDD.cache

    val seed = 42
    val weights = Array(0.1, 0.7, 0.2)
    val Array(trainRDD, valRDD, testRDD) = finalRDD.randomSplit(weights, seed)
    val numClusters = 10
    val numIterations = 20

    val kmeansModel = KMeans.train(trainRDD, numClusters, numIterations)
    val WSSSE = kmeansModel.computeCost(trainRDD)
    val cc = kmeansModel.clusterCenters

    val clusterIdUDF = udf { (x: Vector) =>
      val vector = x
      kmeansModel.predict(vector)
    }
    df2.select(col("asin"), clusterIdUDF(col("features")).as("clusterID")).show()
    println("Within Set Sum of Squared Errors = " + WSSSE)

  }
}