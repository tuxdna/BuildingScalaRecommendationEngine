package chapter04

import org.apache.spark.mllib.clustering.LDA
import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.SparkConf
import org.apache.spark.SparkContext
import java.io.File
import scala.io.Source
import org.apache.spark.rdd.RDD
import org.apache.spark.mllib.feature.HashingTF
import org.apache.spark.mllib.linalg.Vector
import java.util.PriorityQueue
import java.util.Comparator
import java.util.TreeSet
import java.util.TreeMap
import scala.collection.JavaConversions._

object TextLDA {

  def loadData(dataPath: File): Array[(String, Array[String])] = {
    val categories = Array("business", "entertainment", "politics", "sport", "tech")
    val allData = (categories).flatMap { category =>
      val folder = new File(dataPath, category)
      println(folder.getAbsolutePath())
      folder.listFiles.map { docf =>
        val docid = docf.getName().replace(".txt", "")
        println(docf.getAbsolutePath())
        val lines = Source.fromFile(docf, "latin1").getLines.mkString(" ")
        val llines = lines.toLowerCase()
        val words = llines
          .split(" ")
          .flatMap(_.split("(\"|\\'|\\,|\\.|\\?|\\!)+"))
          .filter(_.length() > 1)

        val content = words
        docid -> content
      }
    }
    // allData foreach println
    allData
  }

  def main(args: Array[String]): Unit = {

    val defaultPath = "datasets/bbc"
    val dataPath = new File(if (args.length > 0) args(0) else defaultPath)
    val textData = loadData(dataPath)

    val conf = new SparkConf(false).setMaster("local[2]").setAppName("BBCNews")
    val sc = new SparkContext(conf)

    val documents: RDD[Seq[String]] = sc.parallelize(textData.map(_._2.toSeq).toSeq)
    val hashingTF = new HashingTF()
    val tf: RDD[Vector] = hashingTF.transform(documents)

    // Load and parse the data
    // Index documents with unique IDs
    val corpus = tf.zipWithIndex.map(_.swap).cache()
    // Cluster the documents into three topics using LDA
    val ldaModel = new LDA().setK(5).run(corpus)
    println("Learned topics (as distributions over vocab of " + ldaModel.vocabSize + " words):")
    val topics = ldaModel.topicsMatrix
    val N = 5
    for (topic <- 0 until 5) {
      print("Topic " + topic + ": ")
      val treeMap = new TreeMap[Double, Int]()
      for (word <- 0 until ldaModel.vocabSize) {
        val score = topics(word, topic)
        if (treeMap.keySet().size() < N) treeMap.put(score, word)
        else {
          val low = treeMap.firstKey()
          if (score > low) {
            treeMap.remove(low); treeMap.put(score, word)
          }
        }
      }
      treeMap.keySet() foreach { k =>
        print(s" (${treeMap(k)},$k)")
      }
      println()
    }
  }
}