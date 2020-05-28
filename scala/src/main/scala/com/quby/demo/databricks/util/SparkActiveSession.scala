package com.quby.demo.databricks.util

import org.apache.spark.sql.SparkSession
import org.slf4j.{Logger, LoggerFactory}

trait SparkActiveSession extends RunOnUTC {
  val spark:  SparkSession = SparkSession.builder().getOrCreate()
  val logger: Logger       = LoggerFactory.getLogger(this.getClass.getSimpleName)
}
