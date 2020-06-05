package com.quby.demo.databricks.storage

import com.quby.demo.databricks.schema.{ElectricityPowerSchema, RawSchema}
import org.apache.spark.sql.Dataset

/***
  * Repository is the interface we use to access data from the storage.
  *
  * The two implementations are:
  *  * SampleRepository -> Use this one for generating sample data on-the-fly
  *  * DeltaRepository -> Use this one for accessing data stored on Databricks Delta
  */
trait Repository {

  /***
    * The raw table contains all the IoT signals collected from the devices stored with RawSchema schema.
    * The table is partitioned by utcDate and variableName.
    *
    * FAQ: Why using a single table in long format for all signals?
    *      -> This makes it easy to add more signals without changing the schema.
    * FAQ: Why partition by utcDate and variableName?
    *      -> This are variables with low cardinality that are most used in the queries using raw data.
    * FAQ: Why are utcDateFrom and utcDateTo required parameters?
    *      -> IoT signals are generally unbounded datasets, which grow over time. Specifying a date range
    *         in all queries prevents execution time to grow over time.
    *
    * @param utcDateFrom Date in ISO8601 formatted string (YYYY-MM-DD)
    * @param utcDateTo Date in ISO8601 formatted string (YYYY-MM-DD)
    * @return
    */
  def raw(utcDateFrom: String, utcDateTo: String): Dataset[RawSchema]

  /***
    * This retrieves the electricity power signal extracted from the raw data.
    *
    * @param utcDateFrom Date in ISO8601 formatted string (YYYY-MM-DD)
    * @param utcDateTo Date in ISO8601 formatted string (YYYY-MM-DD)
    * @return
    */
  def electricityPower(utcDateFrom: String, utcDateTo: String): Dataset[ElectricityPowerSchema]

  /***
    * Overwrites a slice of the time series data, assuming it is partitioned by date.
    *
    * FAQ: Why overwriting instead of appending?
    *      -> This enforces idempotency constraint for Batch operations. I.e. re-running a job multiple times
    *         does not create side effects like duplicated data.
    *         For streaming operation it is not necessary to overwrite, since Spark with checkpointing
    *         takes care of executing the transformations exactly once.
    *
    * @param ds Dataset to be stored
    * @param db Database name
    * @param table Table name
    * @param partitions Sequence of partitions to use when creating the table (one of them has to be equal to dateColumnName)
    * @param dateFrom Date from which to overwrite (inclusive)
    * @param dateTo Date to which to overwrite (inclusive)
    * @param dateColumnName Name of the date column used for partitioning
    */
  def overwriteDateRange(ds:             Dataset[_],
                         db:             String,
                         table:          String,
                         partitions:     Seq[String],
                         dateFrom:       String,
                         dateTo:         String,
                         dateColumnName: String = "utcDate")
}
