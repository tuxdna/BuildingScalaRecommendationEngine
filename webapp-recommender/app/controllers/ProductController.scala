package controllers

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

import play.api.mvc.Action
import play.api.mvc.Controller
import util.Database
import util.ReactiveDB

object ProductController extends Controller {
  def list() = Action.async {
    val itemsF = ReactiveDB.allProducts.collect[Seq](100, true)
    itemsF map { items =>
      Ok(views.html.products(items))
    }
  }

  def get(asin: String) = Action.async {
    val itemCursor = ReactiveDB.getByASIN(asin)
    val itemsF = itemCursor.collect[List](1, true)
    itemsF.map { items =>
      val itemOpt = items match {
        case x :: xs => Some(x)
        case _ => None
      }
      Ok(views.html.product(itemOpt))
    }
  }

  def getGroups() = Action.async {
    Future {
      val groups = Database.getAllProductGroups()
      Ok(views.html.product_groups(groups))
    }
  }

  def getForGroup(group: String) = Action.async {
    val itemsF = ReactiveDB.getByGroup(group).collect[Seq](100, false)
    itemsF map { items =>
      Ok(views.html.products(items))
    }
  }
}