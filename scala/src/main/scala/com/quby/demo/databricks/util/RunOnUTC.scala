package com.quby.demo.databricks.util

import java.util.TimeZone
import org.joda.time.DateTimeZone

/**
  * Spark methods displays Timestamp columns always with Local System timezone.
  * Keeping the timezone aligned to UTC makes it easier to write unit tests and 
  * check the correctness of time values regardless of the timezone of the developer's 
  * machine.
  */
trait RunOnUTC {
  DateTimeZone.setDefault(DateTimeZone.UTC)
  TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
}
