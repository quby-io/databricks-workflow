package com.quby.demo.databricks.storage

import com.quby.demo.databricks.schema.{ElectricityPowerSchema, RawSchema, UserRatingSchema}
import org.apache.spark.sql.Dataset

trait Repository {
  def raw(dateFrom: String, dateTo: String): Dataset[RawSchema]

  def electricityPower(dateFrom: String, dateTo: String): Dataset[ElectricityPowerSchema]
  def overwriteDateRange(ds: Dataset[_],
                         db: String,
                         table: String,
                         partitions: Seq[String],
                         dateFrom: String,
                         dateTo: String,
                         dateColumnName: String = "utcDate")

  def userRating(dateFrom: String, dateTo: String): Dataset[UserRatingSchema]
}
