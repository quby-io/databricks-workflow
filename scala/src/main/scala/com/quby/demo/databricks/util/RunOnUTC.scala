package com.quby.demo.databricks.util

import java.util.TimeZone
import org.joda.time.DateTimeZone

/**
  * In order for Spark to convert unix_timestamp to utc_datetime, the application should
  * always run in UTC timezone.
  * Spark methods parses Timestamp columns always with Local System timezone.
  * See documentation for "from_unixtime" and
  */
trait RunOnUTC {
  DateTimeZone.setDefault(DateTimeZone.UTC)
  TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
}
