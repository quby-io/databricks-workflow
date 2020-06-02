package com.quby.demo.databricks.storage

import com.quby.demo.databricks.util.SparkActiveSession
import org.apache.spark.sql.Dataset
import org.apache.spark.sql.types.StructType

object CatalogHelper extends SparkActiveSession {
  import spark.implicits._

  def isDelta(db: String, table: String): Boolean = {
    getTableProperty(db, table, "Provider").toString.toLowerCase == "delta"
  }

  def isHive(db: String, table: String): Boolean = {
    getTableProperty(db, table, "Provider").toString.toLowerCase == "hive"
  }

  def fullTableName(db: String, tableName: String): String = s"${db}.${tableName}"

  def tableExists(db: String, tableName: String): Boolean = spark.catalog.tableExists(db, tableName)

  def databaseExists(database: String): Boolean = spark.catalog.databaseExists(database)

  def getTableProperty(db: String, table: String, property: String): String = {
    if (!tableExists(db, table))
      throw new NoSuchElementException(s"Table ${db}.${table} wasn't found.")

    val propDetails = spark
      .sql(s"DESCRIBE EXTENDED ${db}.${table}")
      .filter($"col_name" === property)
      .collect()

    if (propDetails.isEmpty)
      throw new NoSuchElementException(s"Property ${property} wasn't found in ${table} table.")

    propDetails(0).getString(1)
  }

//
//  def getTableProperties(table: String, properties: String*): Map[String, Boolean] = {
//    if (!tableExists(table))
//      throw new NoSuchElementException(s"Table ${table} wasn't found.")
//
//    var df = spark.sql(s"SHOW TBLPROPERTIES ${table}")
//    if (properties.nonEmpty)
//      df = df.where($"key".isin(properties: _*))
//
//    df.collect
//      .map { r =>
//        (r.getAs[String]("key"), r.getAs[String]("value").toBoolean)
//      }
//      .toMap[String, Boolean]
//  }

  def createDatabaseIfNotExist(database: String): Unit = {
    if (!databaseExists(database)) {
      val sql = s"create database if not exists $database"
      spark.sql(sql)
    }
  }

  def createTableIfNotExists(df:         Dataset[_],
                             database:   String,
                             table:      String,
                             partitions: Seq[String],
                             location:   String): Unit = {
    if (!tableExists(database, table)) {
      val sql = createTableSql(database, table, df.schema, partitions, location)
      spark.sql(sql)
    }
  }

  def createTableSql(database:   String,
                     table:      String,
                     schema:     StructType,
                     partitions: Seq[String],
                     location:   String): String = {
    s"""
    create table if not exists $database.$table (
        ${schema.fields.map(c => s"${c.name} ${c.dataType.sql}").mkString(",\n")}
    )
    using delta
    ${if (partitions.nonEmpty) s"partitioned by (${partitions.mkString(",\n")})" else ""}

    ${if (location != "") s"location '$location/$database.db/$table'" else ""}
    """
  }

  case class HiveCol(name: String, dataType: String)

}
