package com.quby.demo.databricks.schema

import java.sql.Timestamp
import java.time.format.DateTimeFormatter

case class ElectricityPowerSchema(
    userId:  String,
    powerW:  Int,
    ts:      Timestamp,
    utcDate: String
)

object ElectricityPowerSchema {
  def create(
      userId:   String,
      powerW:   Int,
      tsMillis: Long
  ) = {

    val ts      = new Timestamp(tsMillis)
    val utcDate = ts.toLocalDateTime.format(DateTimeFormatter.ISO_DATE)

    ElectricityPowerSchema(
      userId,
      powerW,
      ts,
      utcDate
    )
  }

  def create(
      userId:   String,
      powerW:   Int,
      tsString: String
  ) = {

    val ts      = Timestamp.valueOf(tsString)
    val utcDate = ts.toLocalDateTime.format(DateTimeFormatter.ISO_DATE)

    ElectricityPowerSchema(
      userId,
      powerW,
      ts,
      utcDate
    )
  }

}
