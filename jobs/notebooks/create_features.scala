// Databricks notebook source
// MAGIC %md
// MAGIC # Example job
// MAGIC This notebook will create an example dataset

// COMMAND ----------

import com.databricks.dbutils_v1.DBUtilsHolder.dbutils
import com.quby.demo.databricks.storage.{DeltaRepository, TableNames}
import com.quby.demo.databricks.transformations.{ElectricityPower, PresenceLabel}
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

// DBTITLE 1,Read input data
val raw = repository.raw(utcDateFrom, utcDateTo)

// COMMAND ----------

// DBTITLE 1,Execute transformations
// Extract electricity power measurements from raw IoT data
val power = ElectricityPower.transform(raw)

// COMMAND ----------

// DBTITLE 1,Persist results
// Persist power measurements in it's own table
// We always overwrite a date range, in order to guarantee idempotency constraint
repository.overwriteDateRange(power, featureDb, TableNames.electricityPower, Seq("utcDate"), utcDateFrom, utcDateTo)

// COMMAND ----------

// DBTITLE 1,Execute transformations
// Extract electricity power measurements from raw IoT data
val presence_label = PresenceLabel.transform(raw)

// COMMAND ----------

// DBTITLE 1,Persist results
// Persist presence label measurement in it's own table
// We always overwrite a date range, in order to guarantee idempotency constraint
repository.overwriteDateRange(presence_label, featureDb, TableNames.presenceLabel, Seq("utcDate"), utcDateFrom, utcDateTo)


// COMMAND ----------

// DBTITLE 1,Assert expectations
// Assert the number of users is in expected range

val usersCount = raw
  .select($"userId")
  .distinct()
  .count()

val usersWithPowerCount = repository
  .electricityPower(utcDateFrom, utcDateTo)
  .select($"userId")
  .distinct()
  .count()

assert(usersWithPowerCount >= usersCount * 0.9, "The number of users with power should be at least 90% of the total user count")
assert(usersWithPowerCount <= usersCount, "The number of users with power should be less than or equal to the total user count")

// COMMAND ----------

