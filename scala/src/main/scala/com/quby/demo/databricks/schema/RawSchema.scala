package com.quby.demo.databricks.schema

import java.sql.Timestamp
import java.time.format.DateTimeFormatter

case class RawSchema(
    userId:        String,
    variableName:  String,
    variableValue: String,
    ts:            Timestamp,
    utcDate:       String
)

object RawSchema {
  def create(
      userId:        String,
      variableName:  String,
      variableValue: String,
      tsMillis:      Long
  ) = {
    val ts      = new Timestamp(tsMillis)
    val utcDate = ts.toLocalDateTime.format(DateTimeFormatter.ISO_DATE)
    RawSchema(userId, variableName, variableValue, ts, utcDate)
  }
}
