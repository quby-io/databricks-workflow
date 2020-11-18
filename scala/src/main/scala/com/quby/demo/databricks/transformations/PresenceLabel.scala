package com.quby.demo.databricks.transformations

import com.quby.demo.databricks.schema.{PresenceLabelSchema, RawSchema}
import com.quby.demo.databricks.util.SparkActiveSession
import org.apache.spark.sql.Dataset

/**
  * ElectricityPower contains the transform function to clean and extract the electricity signal from the raw data
  */
object PresenceLabel extends SparkActiveSession {

  import spark.implicits._

  val minPowerW = 0
  val maxPowerW = 50000

  /**
    * Extracts and cleans the electricity power signal from all other IoT signals in the raw table.
    * @param raw Dataset with all IoT signals
    * @return Dataset with a clean electricity power signal
    */
  def transform(raw: Dataset[RawSchema]): Dataset[PresenceLabelSchema] = {
    import org.apache.spark.sql.functions.when

    raw
      .filter($"variableName" === "is_someone_home")
      .withColumn("isSomeoneHome", when($"variableValue" === "true", 1).otherwise(0).cast("int"))
      .select("userId", "ts", "isSomeoneHome", "utcDate")
      .as[PresenceLabelSchema]
  }
}
