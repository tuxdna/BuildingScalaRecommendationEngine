package controllers

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import play.api.mvc.Action
import play.api.mvc.Controller
import util.Database
import util.ReactiveDB
import play.api.libs.json.Json
import play.api.libs.json.JsValue
import util.Recommender
import util.ESClient
import scala.collection.JavaConversions._

object SearchController extends Controller {
  def searchProducts() = Action.async { implicit request =>
    val query = request.getQueryString("q").getOrElse("")
    val searchResponseFuture = ESClient.searchItem(query)
    searchResponseFuture.map { searchResponse =>
      val hitlist = searchResponse.getHits().map { hit =>
        hit.getSource().toMap
      }.toList
      
      Ok(searchResponse.toString()).withHeaders("Content-Type" -> "application/json")
    }
  }
}
