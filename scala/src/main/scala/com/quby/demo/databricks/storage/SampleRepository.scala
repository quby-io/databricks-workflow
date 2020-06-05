package com.quby.demo.databricks.storage

import java.sql.Timestamp

import com.quby.demo.databricks.schema.{ElectricityPowerSchema, RawSchema, UserRatingSchema}
import com.quby.demo.databricks.util.{RunOnUTC, SparkActiveSession}
import org.apache.spark.sql.Dataset
import org.joda.time.{DateTime, Minutes}

class SampleRepository(minutesStep: Int, randomSeed: Int = 42)
    extends Repository
    with SparkActiveSession
    with RunOnUTC {
  import spark.implicits._

  case class SampleTimestamp(i: Int, ts: DateTime)

  /***
    * Creates a range of timestamps for the data range with minutesStep resolution
    * @param dateFrom UTC Date from (inclusive) in format YYYY-MM-DD
    * @param dateTo UTC Date to (inclusive) in format YYYY-MM-DD
    * @return
    */
  def range(dateFrom: String, dateTo: String) = {

    val start = DateTime.parse(dateFrom)
    val end   = DateTime.parse(dateTo).plusDays(1)

    val minutesCount = Minutes.minutesBetween(start, end).getMinutes() / minutesStep

    (0 until minutesCount.toInt).map(x => SampleTimestamp(x, start.plusMinutes(x * minutesStep)))
  }

  def raw(dateFrom: String, dateTo: String) = {

    val rand = new scala.util.Random(randomSeed)

    range(dateFrom, dateTo)
      .flatMap(
        t =>
          Seq(
            RawSchema.create("user-1", "temperature_c", rand.nextInt(30).toString, t.ts.getMillis),
            RawSchema.create("user-2", "temperature_c", rand.nextInt(32).toString, t.ts.getMillis),
            RawSchema.create("user-1", "power_w", rand.nextInt(250).toString, t.ts.getMillis),
            RawSchema.create("user-2", "power_w", rand.nextInt(600).toString, t.ts.getMillis)
        ))
      .toDS()
  }

  override def electricityPower(dateFrom: String, dateTo: String): Dataset[ElectricityPowerSchema] = {
    val rand = new scala.util.Random(randomSeed)
    range(dateFrom, dateTo)
      .flatMap(
        seed =>
          Seq(
            ElectricityPowerSchema.create("user-1", rand.nextInt(250), seed.ts.getMillis),
            ElectricityPowerSchema.create("user-2", rand.nextInt(600), seed.ts.getMillis)
        ))
      .toDS()
  }

  override def overwriteDateRange(ds:             Dataset[_],
                                  db:             String,
                                  table:          String,
                                  partitions:     Seq[String],
                                  dateFrom:       String,
                                  dateTo:         String,
                                  dateColumnName: String): Unit = {
    logger.info(s"overwriteDateRange method called for $db.$table for range $dateFrom <= $dateColumnName <= $dateTo")
    logger.warn("This is a sample repository, the data will not be persisted.")
  }

  override def userRating(dateFrom: String, dateTo: String): Dataset[UserRatingSchema] = {
    val rand = new scala.util.Random()
    var ratingList = List.empty[UserRatingSchema]
    range(dateFrom, dateTo)
      .map(
        seed => {
          (1 to 100) foreach (i => {
            ratingList = ratingList :+ UserRatingSchema.create(i, 1, rand.nextInt(5) + 1, seed.ts.getMillis)
            ratingList = ratingList :+ UserRatingSchema.create(i, 2, rand.nextInt(5) + 1, seed.ts.getMillis)
            ratingList = ratingList :+ UserRatingSchema.create(i, 3, rand.nextInt(5) + 1, seed.ts.getMillis)
            ratingList = ratingList :+ UserRatingSchema.create(i, 4, rand.nextInt(5) + 1, seed.ts.getMillis) // rate for a random item
          }
            )

          (1 to 80) foreach (i => ratingList = ratingList :+ UserRatingSchema.create(i, 5, rand.nextInt(5) + 1, seed.ts.getMillis))
        }
      )
    ratingList.toDS()

  }
}
