package com.quby.demo.databricks.util

import org.apache.spark.sql.SparkSession
import org.slf4j.{Logger, LoggerFactory}

/***
  * Extend this trait for having quick access to current SparkSession and logger.
  */
trait SparkActiveSession extends RunOnUTC {
  val spark:  SparkSession = SparkSession.builder().getOrCreate()
  val logger: Logger       = LoggerFactory.getLogger(this.getClass.getSimpleName)
}
