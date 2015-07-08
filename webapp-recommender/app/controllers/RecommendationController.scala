package controllers

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

import play.api.mvc.Action
import play.api.mvc.Controller
import util.Database
import util.ReactiveDB

object RecommendationController extends Controller {

  def topRated() = Action.async {
    val itemsF = ReactiveDB.topRatedProducts.collect[Seq](100, true)
    itemsF map { items =>
      Ok(views.html.top_rated_products(items))
    }
  }

  def mostPopular() = Action.async {
    val itemsF = ReactiveDB.mostPopularProducts.collect[Seq](100, true)
    itemsF map { items =>
      Ok(views.html.most_popular_products(items))
    }
  }


}