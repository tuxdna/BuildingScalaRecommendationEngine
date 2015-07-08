package chapter04

import java.io.File

import org.apache.spark.SparkConf
import org.apache.spark.SparkContext
import org.apache.spark.mllib.classification.NaiveBayes
import org.apache.spark.mllib.util.MLUtils

object TVNewsChannelsNaiveBayes {
  def main(args: Array[String]) {
    val dataPath = "datasets/TVData"
    val fileNames = List("BBC.txt", "CNNIBN.txt", "CNN.txt", "NDTV.txt", "TIMESNOW.txt")

    // now that we have encoded features into numeric values,
    // lets create RDD of labeled points for classification
    val conf = new SparkConf(false).setMaster("local[2]").setAppName("TVNewsChannels")
    val sc = new SparkContext(conf)

    val allInstances = fileNames.map { fileName =>
      val dpath = new File(dataPath)
      val fpath = new File(dpath, fileName)
      MLUtils.loadLibSVMFile(sc, fpath.getAbsolutePath())
    }

    val data = allInstances.reduce { (r1, r2) => r1.union(r2) }

    // test / train split
    val splits = data.randomSplit(Array(0.6, 0.4), seed = 11L)
    val trainData = splits(0).cache()
    val testData = splits(1)

    val NLambdas = 10

    val output = (1 to NLambdas) map { i =>
      val lambda = 1.0 * i / NLambdas

      val model = NaiveBayes.train(trainData, lambda = lambda)

      val trainResult = Utils.evaluate(model, trainData)
      val testResult = Utils.evaluate(model, testData)
      (lambda, trainResult, testResult)
    }
    Utils.writeToFile("output/TVNewsOutput-NaiveBayes.csv", output)
  }
}