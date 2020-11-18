package com.quby.demo.databricks.storage

import com.quby.demo.databricks.schema.{ElectricityPowerSchema, PresenceLabelSchema, RawSchema}
import com.quby.demo.databricks.util.{RunOnUTC, SparkActiveSession}
import org.apache.spark.sql.Dataset
import org.joda.time.{DateTime, Minutes}

class SampleRepository(minutesStep: Int, randomSeed: Int = 42)
    extends Repository
    with SparkActiveSession
    with RunOnUTC {
  import spark.implicits._

  case class SampleTimestamp(i: Int, ts: DateTime)

  /**
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
        t => {
          val user_1_power = rand.nextInt(250)
          val user_2_power = rand.nextInt(600)

          Seq(
            RawSchema.create("user-1", "temperature_c", rand.nextInt(30).toString, t.ts.getMillis),
            RawSchema.create("user-2", "temperature_c", rand.nextInt(32).toString, t.ts.getMillis),
            RawSchema.create("user-1", "power_w", user_1_power.toString, t.ts.getMillis),
            RawSchema.create("user-2", "power_w", user_2_power.toString, t.ts.getMillis),
            RawSchema.create("user-1", "is_someone_home", (user_1_power > 200).toString, t.ts.getMillis)
          )
        })
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

  override def presenceLabels(dateFrom: String, dateTo: String): Dataset[PresenceLabelSchema] = {
    val rand = new scala.util.Random(1)
    range(dateFrom, dateTo)
      .flatMap(
        seed =>
          Seq(
            PresenceLabelSchema.create("user-1", "isSomeoneHome", rand.nextInt(1).toString, seed.ts.getMillis),
            PresenceLabelSchema.create("user-2", "isSomeoneHome", rand.nextInt(0).toString, seed.ts.getMillis)
          ))
      .toDS()
  }
}
