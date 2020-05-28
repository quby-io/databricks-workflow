package com.quby.demo.databricks.transformations

import com.quby.demo.databricks.schema.{RawSchema, ElectricityPowerSchema}
import com.quby.demo.databricks.util.SparkActiveSession
import org.apache.spark.sql.Dataset

object ElectricityPower extends SparkActiveSession {

  import spark.implicits._

  val minPowerW = 0
  val maxPowerW = 50000

  /***
    * Extracts and cleans the electricity power signal from all other IoT signals in the raw table.
    * @param raw Dataset with all IoT signals
    * @return Dataset with a clean electricity power signal
    */
  def transform(raw: Dataset[RawSchema]): Dataset[ElectricityPowerSchema] = {
    raw
      .filter($"variableName" === "power_w")
      .withColumn("powerW", $"variableValue".cast("int"))
      .filter($"powerW".between(minPowerW, maxPowerW))
      .select("userId", "ts", "powerW", "utcDate")
      .as[ElectricityPowerSchema]
  }
}
