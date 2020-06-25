package com.quby.demo.databricks.util

import org.joda.time.DateTime
import org.joda.time.format.ISODateTimeFormat

/**
  * Contains helper functions to be used by Notebooks.
  */
object JobUtils extends RunOnUTC {

  /**
    * Returns the provided date if nonempty, otherwise returns the current UTC date.
    * @param utcDate Date in ISO8601 formatted string (YYYY-MM-DD) or an empty string
    * @return Date in ISO8601 formatted string (YYYY-MM-DD)
    */
  def getOrTodayUTC(utcDate: String): String = {
    if (utcDate.isEmpty)
      ISODateTimeFormat.date().print(DateTime.now())
    else {
      DateTime.parse(utcDate)
      utcDate
    }
  }

  /**
    * Parses the date string, subtracts a number of days and formats the date back to string.
    *
    * FAQ: Why do we use String type instead of Date type?
    *      -> The parameters in Databricks notebooks are passed as Strings, and we partition the IoT data
    *         by utcDate (String type) because this produces a flat partitioning "YYYY-MM-DD" structure as opposed to a  
    *         hierarchical partitioning produced by Date types (YYYY/MM/DD).
    * @param utcDate Date in ISO8601 formatted string (YYYY-MM-DD).
    * @param days Number of days to subtract
    * @return Date in ISO8601 formatted string (YYYY-MM-DD)
    */
  def subtractDays(utcDate: String, days: Int): String = {
    val date = DateTime.parse(utcDate).minusDays(days)
    ISODateTimeFormat.date().print(date)
  }
}
