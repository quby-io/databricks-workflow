package com.quby.demo.databricks.util

import java.io.File
import java.nio.file.{Path, Paths}
import java.util.UUID.randomUUID

import org.apache.spark.sql.SparkSession

object SharedSparkSession {

  lazy val spark: SparkSession = _spark

  val uuidString = randomUUID().toString
  private val tempPath:        Path   = Paths.get(System.getProperty("java.io.tmpdir"))
  private val checkpointPath:  String = new File(tempPath.toFile, "checkpoints").getAbsolutePath

  def _spark: SparkSession = {
    println("Creating Spark Session")
    val session = SparkSession
      .builder()
      .appName("test")
      .master("local[*]")
      .config("spark.speculation", "false")
      .config("spark.ui.enabled", "false")
      .config("spark.ui.showConsoleProgress", "false")
      .config("spark.sql.shuffle.partitions", 4) // This makes tests faster
      .config("spark.sql.legacy.allowCreatingManagedTableUsingNonemptyLocation", "true")
      .getOrCreate()

    session.sparkContext.setCheckpointDir(checkpointPath)
    session
  }
}
