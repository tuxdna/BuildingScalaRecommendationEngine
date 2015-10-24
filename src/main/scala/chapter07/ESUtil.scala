package chapter07

import com.sksamuel.elastic4s.ElasticClient
import com.sksamuel.elastic4s.ElasticDsl._
import scala.concurrent.Future
import org.elasticsearch.action.search.SearchResponse
import com.sksamuel.elastic4s.source.Indexable

case class AmazonItem(
                       asin: String,
                       title: String,
                       group: String,
                       salesRank: Long,
                       averageRating: Double,
                       categories: String)

object ESUtil {

  object ESConfig {
    val server = "127.0.0.1"
    val port = 9300
    val productsIndex = "products_index"
  }

  lazy val client = ElasticClient.remote(ESConfig.server, ESConfig.port)

  def deleteProducts() = {
    client.execute {
      deleteIndex(ESConfig.productsIndex)
    }
  }

  def insertItem(item: AmazonItem) = {
    client.execute {
      index into (ESConfig.productsIndex) / "items" fields(
        "asin" -> item.asin,
        "title" -> item.title,
        "group" -> item.group,
        "salesRank" -> item.salesRank,
        "averageRating" -> item.averageRating,
        "categories" -> item.categories)
    }
  }

  /**
   * Search for an item in the index
   * @param q
   * @return
   */
  def searchItem(q: String): Future[SearchResponse] = {
    val productsIndex = ESConfig.productsIndex
    val rs = client.execute {
      search in productsIndex / "items" query q
    }
    rs
  }

}
