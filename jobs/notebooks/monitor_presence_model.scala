// Databricks notebook source
// MAGIC %md
// MAGIC %md
// MAGIC # Model performance monitoring
// MAGIC 
// MAGIC 1. Use mlflow api to get the experiment by name/id
// MAGIC 
// MAGIC 2. get latest 2 runs and compare the values
// MAGIC 
// MAGIC 3. assert if latest run value deviates more than threshold value

// COMMAND ----------

// MAGIC %python

// MAGIC import mlflow
// MAGIC import pandas as pd
// MAGIC 
// MAGIC experiment = mlflow.get_experiment_by_name("/PresencePredictions")
// MAGIC 
// MAGIC runs = mlflow.search_runs(experiment_ids=experiment.experiment_id, max_results=50)
// MAGIC 
// MAGIC runs



// COMMAND ----------

// MAGIC %python

// MAGIC runs_latest2 = runs[runs['tags.mlflow.runName'] == 'presence_experiment_run'] \
// MAGIC   .loc[runs.groupby("params.date").start_time.idxmax()] \
// MAGIC   .sort_values("params.date", ascending=False) \
// MAGIC .head(2)
// MAGIC 
// MAGIC runs_latest2


// COMMAND ----------

// MAGIC %python

// MAGIC auROC_delta = abs(runs_latest2["metrics.auROC"].diff().iloc[1])
// MAGIC assert(auROC_delta < .05)


// COMMAND ----------

// MAGIC %python
// MAGIC runs_latest_month = runs[runs['tags.mlflow.runName'] == 'presence_experiment_run'] \
// MAGIC   .loc[runs.groupby("params.date").start_time.idxmax()] \
// MAGIC   .sort_values("params.date", ascending=True) \
// MAGIC .head(30)

// MAGIC runs_latest_month


// COMMAND ----------

// MAGIC %python

// MAGIC display(runs_latest_month)