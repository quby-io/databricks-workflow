package com.quby.demo.databricks.util

import org.joda.time.DateTime
import org.joda.time.format.ISODateTimeFormat

object JobUtils {
  // TODO add tests
  def getOrTodayUTC(utcDate: String) = {
    if (utcDate.isEmpty())
      ISODateTimeFormat.date().print(DateTime.now())
    else {
      DateTime.parse(utcDate)
      utcDate
    }
  }

  def subtractDays(utcDate: String, days: Int) = {
    val date = DateTime.parse(utcDate).minusDays(days)
    ISODateTimeFormat.date().print(date)
  }
}
