package chapter05

import java.io.File
import java.io.PrintWriter

import org.apache.hadoop.conf.Configuration
import org.apache.spark.SparkConf
import org.apache.spark.SparkContext
import org.apache.spark.rdd.RDD.rddToPairRDDFunctions
import org.bson.BSONObject

import com.mongodb.hadoop.MongoInputFormat
import com.twitter.algebird.CMSHasherImplicits.CMSHasherLong
import com.twitter.algebird.MapMonoid
import com.twitter.algebird.TopPctCMS

object TopKProducts {

  def main(args: Array[String]) {

    val conf = new SparkConf(false).setMaster("local[2]").setAppName("TopKProducts")
    val sc = new SparkContext(conf)

    val config = new Configuration()
    config.set("mongo.input.uri", "mongodb://127.0.0.1:27017/amazon_dataset.reviews")
    config.set("mongo.output.uri", "mongodb://127.0.0.1:27017/amazon_dataset.top_k")

    val mongoRDD = sc.newAPIHadoopRDD(config,
      classOf[MongoInputFormat],
      classOf[Object],
      classOf[BSONObject])

    // Count Min Sketch parameters
    val DELTA = 1E-3
    val EPS = 0.01
    val SEED = 1
    val PERC = 0.001
    val TOPK = 10 // K highest frequency elements to take

    val productIdRDD = mongoRDD.map { arg =>
      val (objId, bsonObj) = arg
      val asin = bsonObj.get("productId").toString.toDouble.toLong
      asin
    }

    val cms = TopPctCMS.monoid[Long](EPS, DELTA, SEED, PERC)
    val mm = new MapMonoid[Long, Int]()

    //    var globalCMS = cms.zero
    //    var globalExact = Map[Long, Int]()

    val approxTopProducts = productIdRDD.mapPartitions(ids => {
      ids.map(id => cms.create(id))
    }).reduce(_ ++ _)

    val heavyHitters = approxTopProducts.heavyHitters

    val exactTopProducts = productIdRDD.map(id => (id, 1))
      .reduceByKey((a, b) => a + b)

    val topHitters = exactTopProducts.top(TOPK).toList

    val writer = new PrintWriter(new File("topK.txt"))
    writer.println("Heavy Hitters:")
    heavyHitters.foreach { hh =>
      writer.println(hh)
    }
    writer.println("Top Hitters:")
    writer.println(topHitters)
    writer.close()
  }
}
