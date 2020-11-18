package com.quby.demo.databricks.schema

import java.sql.Timestamp
import java.time.format.DateTimeFormatter

case class PresenceLabelSchema(
                      userId:        String,
                      isSomeoneHome:  Int,
                      ts:            Timestamp,
                      utcDate:       String
                    )

object PresenceLabelSchema {
  def create(
              userId:        String,
              isSomeoneHome:  Int,
              tsMillis:      Long
            ) = {
    val timeStamp  = new Timestamp(tsMillis)
    val utcDate = timeStamp.toLocalDateTime.format(DateTimeFormatter.ISO_DATE)
    PresenceLabelSchema(userId, isSomeoneHome, timeStamp, utcDate)
  }
}


