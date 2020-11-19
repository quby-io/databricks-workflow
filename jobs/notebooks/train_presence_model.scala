// Databricks notebook source
// MAGIC %md
// MAGIC # Example job
// MAGIC This notebook will create an example dataset

// COMMAND ----------

import com.databricks.dbutils_v1.DBUtilsHolder.dbutils
import com.quby.demo.databricks.storage.DeltaRepository
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
val power = repository.electricityPower(utcDateFrom, utcDateTo)

// COMMAND ----------
// DBTITLE 1,Preprocess data
val ds = power.join(presenceLabels, Seq("userId", "ts"))



// COMMAND ----------
// DBTITLE 1,Train model: Split data into training and test sets

import org.apache.spark.ml.Pipeline
import org.apache.spark.ml.classification.DecisionTreeClassifier
import org.apache.spark.ml.feature.VectorAssembler


val Array(training, test) = ds.randomSplit(Array(0.6, 0.4), seed = 11L)
training.cache()

val assembler = new VectorAssembler()
  .setInputCols(Array("powerW"))
  .setOutputCol("features")

val dtc = new DecisionTreeClassifier().setLabelCol("isSomeoneHome")
val pipeline = new Pipeline().setStages(Array(assembler, dtc))

val model = pipeline.fit(training)

// COMMAND ----------
// DBTITLE 1,Transform model
val predicted = model.transform(test)

display(predicted)

// COMMAND ----------
// DBTITLE 1,Use mlFlow experiment for model tracking

import org.mlflow.tracking.ActiveRun
import org.mlflow.tracking.MlflowContext

val mlflowContext = new MlflowContext()

val experimentName = "/PresencePredictions"
val client = mlflowContext.getClient()

if(!client.getExperimentByName(experimentName).isPresent()) {
  client.createExperiment(experimentName)
}

mlflowContext.setExperimentName(experimentName)

val run = mlflowContext.startRun("presence_experiment_run")




// COMMAND ----------
// DBTITLE 1,log metrics

import org.apache.spark.mllib.evaluation.BinaryClassificationMetrics

val predictionAndLabels = predicted.select('isSomeoneHome.cast("Double"), 'prediction).rdd
  .map(row =>
    (row.getAs[Double]("prediction"), row.getAs[Double]("isSomeoneHome"))
  )

// Instantiate metrics object
val metrics = new BinaryClassificationMetrics(predictionAndLabels)

val auROC = metrics.areaUnderROC
run.logMetric("auROC", auROC)

val utcDate = JobUtils.getOrTodayUTC("")
run.logParam("date", utcDate)


// COMMAND ----------
// DBTITLE 1,Finish running experiment

run.endRun()