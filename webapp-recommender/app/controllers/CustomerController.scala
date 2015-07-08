package controllers

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import play.api.mvc.Action
import play.api.mvc.Controller
import util.ReactiveDB

object CustomerController extends Controller {
  def list() = Action.async {

    val itemsF = ReactiveDB.allCustomers().collect[Seq](100, true)
    itemsF map { items =>
      Ok(views.html.customers(items))
    }
    //
    //    Future {
    //      Ok(views.html.customers())
    //    }
  }

  def get(number: Int) = Action.async {
    val itemCursor = ReactiveDB.getCustomerByNumber(number)
    val itemsF = itemCursor.collect[List](1, true)
    itemsF.map { items =>
      val itemOpt = items match {
        case x :: xs => Some(x)
        case _ => None
      }
      Ok(views.html.customer(itemOpt))
    }
  }
}
