package com.quby.demo.databricks.schema

import java.sql.Timestamp
import java.time.format.DateTimeFormatter

case class UserRatingSchema(
                             userId: Int,
                             itemId: Int,
                             rating: Int,
                             ts: Timestamp,
                             utcDate: String
                           )

object UserRatingSchema {
  def create(
              userId: Int,
              itemId: Int,
              rating: Int,
              tsMillis: Long
            ) = {

    val ts = new Timestamp(tsMillis)
    val utcDate = ts.toLocalDateTime.format(DateTimeFormatter.ISO_DATE)

    UserRatingSchema(
      userId,
      itemId,
      rating,
      ts,
      utcDate
    )
  }

  def create(
              userId: Int,
              itemId: Int,
              rating: Int,
              tsString: String
            ) = {

    val ts = Timestamp.valueOf(tsString)
    val utcDate = ts.toLocalDateTime.format(DateTimeFormatter.ISO_DATE)

    UserRatingSchema(
      userId,
      itemId,
      rating,
      ts,
      utcDate
    )
  }

}
