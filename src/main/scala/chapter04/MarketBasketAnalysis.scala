package chapter04

import scala.io.Source
import org.apache.spark.SparkConf
import org.apache.spark.SparkContext
import org.apache.spark.mllib.fpm.FPGrowth

object MarketBasketAnalysis {

  def loadData(dataFile: String): Seq[Array[String]] = {
    val src = Source.fromFile(dataFile)
    val lines = src.getLines
    val header = lines.next().split(",").map(_.trim())
    // println(header.toList)
    val data = lines.map { l =>
      val elems = l.split(",").map(_.trim())
      // println(elems.toList)
      elems.zipWithIndex.filter(_._1.equals("true")).map { x =>
        val (v, i) = x
        header(i)
      }
    }.filter(_.length > 0).toSeq
    data.toList
  }

  
  def main(args: Array[String]): Unit = {
    val defaultFile = "datasets/marketbasket.csv"
    val dataFile = if (args.length > 0) args(0) else defaultFile
    val data = loadData(dataFile)
    val conf = new SparkConf(false).setMaster("local[2]").setAppName("MarketBasket")
    val sc = new SparkContext(conf)
    val transactions = sc.parallelize(data).cache()
    val fpg = new FPGrowth().setMinSupport(0.05)
    val model = fpg.run(transactions)
    // only those itemsets which have 2 or more items together
    model.freqItemsets.filter(_.items.size > 1).collect().foreach { itemset =>
      println(itemset.items.mkString("[", ",", "]") + ", " + itemset.freq)
    }
  }
}





