// Databricks notebook source
// MAGIC %md
// MAGIC # Initialization job
// MAGIC 
// MAGIC This notebook will create the raw database with some sample data.

// COMMAND ----------

// DBTITLE 1,Imports
import com.databricks.dbutils_v1.DBUtilsHolder.dbutils
import com.quby.demo.databricks.storage.{DeltaRepository, SampleRepository, TableNames}
import com.quby.demo.databricks.util.JobUtils
import org.apache.spark.sql.SparkSession

implicit val spark = SparkSession.builder().getOrCreate()
import spark.implicits._

// COMMAND ----------

// DBTITLE 1,Input variables
dbutils.widgets.text("environment", "")
dbutils.widgets.text("utcDate", "")
dbutils.widgets.text("days", "1")
dbutils.widgets.text("rawDb", "")
dbutils.widgets.text("featureDb", "")

val environment = dbutils.widgets.get("environment")
val jobName = dbutils.widgets.get("jobName")
val days = dbutils.widgets.get("days").toInt
val rawDb = dbutils.widgets.get("rawDb")
val featureDb = dbutils.widgets.get("featureDb")

val utcDateTo = JobUtils.getOrTodayUTC(dbutils.widgets.get("utcDate"))
val utcDateFrom = JobUtils.subtractDays(utcDateTo, days)

// COMMAND ----------

// DBTITLE 1,Define repositories
// This repository generates sample data
val sampleRepository = new SampleRepository(15)

// This repository can read and write to actual storage
val deltaRepository = new DeltaRepository(rawDb, featureDb)

// COMMAND ----------

// DBTITLE 1,Create sample raw data
val raw = sampleRepository.raw(utcDateFrom, utcDateTo)
display(raw)

// COMMAND ----------

// DBTITLE 1,Persist sample raw data into storage
deltaRepository.overwriteDateRange(raw, rawDb, TableNames.raw, Seq("utcDate", "variableName"), utcDateFrom, utcDateTo)