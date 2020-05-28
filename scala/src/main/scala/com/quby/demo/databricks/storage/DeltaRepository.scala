package com.quby.demo.databricks.storage

import com.quby.demo.databricks.schema.{ElectricityPowerSchema, RawSchema}
import com.quby.demo.databricks.util.SparkActiveSession
import org.apache.spark.sql.Dataset

class DeltaRepository(rawDb: String, featureDb: String) extends Repository with SparkActiveSession {

  import spark.implicits._

  def read(db: String, table: String, dateFrom: String, dateTo: String) = {
    spark
      .table(s"$db.$table")
      .filter('utcDate.between(dateFrom, dateTo))
  }

  def raw(dateFrom: String, dateTo: String) = {
    read(rawDb, TableNames.raw, dateFrom, dateTo)
      .as[RawSchema]
  }

  override def electricityPower(dateFrom: String, dateTo: String): Dataset[ElectricityPowerSchema] = {
    read(featureDb, TableNames.electricityPower, dateFrom, dateTo)
      .as[ElectricityPowerSchema]
  }

  override def overwriteDateRange(ds:             Dataset[_],
                                  db:             String,
                                  table:          String,
                                  partitions:     Seq[String],
                                  dateFrom:       String,
                                  dateTo:         String,
                                  dateColumnName: String = "utcDate") = {

    if (!partitions.contains(dateColumnName))
      throw new UnsupportedOperationException(
        "In order to overwrite a range, the table must be partitioned by that column")

    CatalogHelper.createDatabaseIfNotExist(db)
    CatalogHelper.createTableIfNotExists(ds, db, table, partitions, "")

    ds.write
      .format("delta")
      .mode("overwrite")
      .option("replaceWhere", s"$dateColumnName >= '$dateFrom' AND $dateColumnName <= '$dateTo'")
      .save(CatalogHelper.getTableProperty(db, table, "Location"))
  }

}
