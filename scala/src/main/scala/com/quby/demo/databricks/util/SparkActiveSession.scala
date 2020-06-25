package com.quby.demo.databricks.util

import org.apache.spark.sql.SparkSession
import org.slf4j.{Logger, LoggerFactory}

/**
  * Extend this trait for having quick access to current SparkSession, logger and sets the current
  * timezone on UTC (See FAQ on RunOnUtc trait).
  * All transformation classes inherit SparkActiveSession for convenience.
  *
  * FAQ: Why do we set current timezone on UTC for all transformations?
  *      --> Spark methods displays Timestamp columns always with Local System timezone.
  *          Keeping the timezone aligned to UTC makes it easier to write unit tests and 
  *          check the correctness of time values regardless of the timezone of the developer's 
  *          machine.
  */
trait SparkActiveSession extends RunOnUTC {
  val spark:  SparkSession = SparkSession.builder().getOrCreate()
  val logger: Logger       = LoggerFactory.getLogger(this.getClass.getSimpleName)
}
