package util

import java.text.SimpleDateFormat
import java.util.Calendar

object DateUtils {

  val sdf = new SimpleDateFormat("yyyy-MM")
  val sdfFull = new SimpleDateFormat("yyyy-MM-dd")

  def dateFor(year: Int, month: Int): java.util.Date = {
    val dt = "%4d-%2d".format(year, month)
    val c = Calendar.getInstance()
    c.setTime(sdf.parse(dt))
    c.getTime()
  }

  def dateFor(year: Int, month: Int, day: Int): java.util.Date = {
    val c = Calendar.getInstance()
    c.set(year, month, day)
    c.getTime()
  }

  def oneMonthAfter(year: Int, month: Int): java.util.Date = {
    val dt = "%4d-%2d".format(year, month)
    val c = Calendar.getInstance()
    // c.set(field, value)
    c.setTime(sdf.parse(dt))
    c.add(Calendar.MONTH, 1) // number of months to add
    c.getTime()
  }

  def addOneMonth(d: java.util.Date): java.util.Date = {
    val c = Calendar.getInstance()
    c.set(d.getYear(), d.getMonth(), d.getDate())
    c.add(Calendar.DATE, 31) // add 31 days
    c.getTime()
  }

}