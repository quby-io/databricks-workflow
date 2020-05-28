package com.quby.demo.databricks

import com.quby.demo.databricks.util.{RunOnUTC, SharedSparkSession}
import org.scalatest.FlatSpec

trait BaseTest extends FlatSpec with RunOnUTC
{
  val spark = SharedSparkSession.spark
}
