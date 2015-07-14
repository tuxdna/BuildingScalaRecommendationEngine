package util

import scala.Option.option2Iterable
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import com.twitter.algebird.CMSHasherImplicits.CMSHasherLong
import com.twitter.algebird.MapMonoid
import com.twitter.algebird.TopPctCMS
import play.api.libs.iteratee.Iteratee
import reactivemongo.bson.BSONDouble
import reactivemongo.bson.BSONDocument
import scala.concurrent.Await
import scala.concurrent._
import scala.concurrent.duration._
import com.twitter.algebird.TopCMS
import reactivemongo.bson.BSONString

object Recommender {

  def findTrendingCustomersFor(year: Int, month: Int): Future[List[String]] = {
    val cursor = ReactiveDB.oneMonthReviewsFrom(year, month)
    val enumerator = cursor.enumerate()
    val itf: Iteratee[BSONDocument, Map[String, Long]] = Iteratee.fold(Map[String, Long]()) { (a, doc: BSONDocument) =>
      val itemId = doc.get("customer").map { _.asInstanceOf[BSONString].value }.get
      val voteCount = doc.get("votes").map { _.asInstanceOf[BSONDouble].value.toLong }.get
      // println(s"itemId: $itemId, voteCount: $voteCount")
      val newVotes = a.get(itemId).getOrElse(0L)
      a + (itemId -> newVotes)
    }

    val finalCounts = enumerator |>>> itf
    finalCounts.map { cms =>
      cms.toList.sortBy(-_._2).map(_._1).take(10)
    }
  }

  def findTrendingProductsFor(year: Int, month: Int): Future[List[Long]] = {
    val cursor = ReactiveDB.oneMonthReviewsFrom(year, month)
    // cursor.collect[Seq](100, false) foreach println

    if (false) {
      // Count Min Sketch parameters
      val DELTA = 1E-3
      val EPS = 0.01
      val SEED = 1
      val PERC = 0.001
      val TOPK = 10 // K highest frequency elements to take
      val cms = TopPctCMS.monoid[Long](EPS, DELTA, SEED, PERC)
      val enumerator = cursor.enumerate()
      val itf: Iteratee[BSONDocument, TopCMS[Long]] = Iteratee.fold(cms.zero) { (a, doc: BSONDocument) =>
        val itemId = doc.get("productId").map { _.asInstanceOf[BSONDouble].value.toLong }.get
        val voteCount = doc.get("votes").map { _.asInstanceOf[BSONDouble].value.toLong }.get
        // println(s"itemId: $itemId, voteCount: $voteCount")
        val b = cms.create(itemId)
        val c = b + (itemId, voteCount)
        a ++ c
      }
      val finalCMS = enumerator |>>> itf
      finalCMS.map { cms =>
        cms.heavyHitters.toList
      }
    } else {
      val enumerator = cursor.enumerate()
      val itf: Iteratee[BSONDocument, Map[Long, Long]] = Iteratee.fold(Map[Long, Long]()) { (a, doc: BSONDocument) =>
        val itemId = doc.get("productId").map { _.asInstanceOf[BSONDouble].value.toLong }.get
        val voteCount = doc.get("productId").map { _.asInstanceOf[BSONDouble].value.toLong }.get
        // println(s"itemId: $itemId, voteCount: $voteCount")
        val newVotes = a.get(itemId).getOrElse(0L)
        a + (itemId -> newVotes)
      }

      val finalCounts = enumerator |>>> itf
      finalCounts.map { cms =>
        cms.toList.sortBy(-_._2).map(_._1).take(10)
      }

    }
  }

  def main(args: Array[String]) {
    // val tpF = findTrendingProductsFor(2005, 0)
    val tpF = findTrendingCustomersFor(2005, 0)
    for (tp <- tpF) {
      println(tp)
    }

    Await.ready(tpF, 1 minute)
  }

}