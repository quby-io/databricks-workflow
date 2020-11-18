// Databricks notebook source
// MAGIC %md
// MAGIC # Example job
// MAGIC This notebook will create an example dataset

// COMMAND ----------

import com.databricks.dbutils_v1.DBUtilsHolder.dbutils
import com.quby.demo.databricks.storage.{DeltaRepository, SampleRepository, TableNames}
import com.quby.demo.databricks.transformations.ElectricityPower
import com.quby.demo.databricks.util.JobUtils
import org.apache.spark.sql.SparkSession

// COMMAND ----------

// DBTITLE 1,Input variables
dbutils.widgets.text("environment", "")
dbutils.widgets.text("jobName", "")
dbutils.widgets.text("utcDate", "")
dbutils.widgets.text("days", "1")
dbutils.widgets.text("rawDb", "staging")
dbutils.widgets.text("featureDb", "")

val environment = dbutils.widgets.get("environment")
val jobName = dbutils.widgets.get("jobName")
val days = dbutils.widgets.get("days").toInt
val rawDb = dbutils.widgets.get("rawDb")
val featureDb = dbutils.widgets.get("featureDb")

val utcDateTo = JobUtils.getOrTodayUTC(dbutils.widgets.get("utcDate"))
val utcDateFrom = JobUtils.subtractDays(utcDateTo, days)

implicit val spark = SparkSession.builder().getOrCreate()
import spark.implicits._

// COMMAND ----------

// DBTITLE 1,Define repository
// Use the DeltaRepository to access data from Delta storage tables
val repository = new DeltaRepository(rawDb, featureDb)

// Use the SampleRepository to generate sample data
// val repository = new SampleRepository(15)

// COMMAND ----------

// DBTITLE 1,Read labels
val presenceLabels = repository.presenceLabels(utcDateFrom, utcDateTo)

// COMMAND ----------

// DBTITLE 1,Execute transformations
// Extract electricity power measurements from raw IoT data
val power = repository.power(utcDateFrom, utcDateTo)

// COMMAND ----------

val ds = power.join(presenceLabels, Seq("uuid", "ts"))




// COMMAND ----------

