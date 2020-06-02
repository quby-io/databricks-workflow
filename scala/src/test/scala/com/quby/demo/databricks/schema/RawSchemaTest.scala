package com.quby.demo.databricks.schema

import java.sql.Timestamp
import com.quby.demo.databricks.BaseTest

class RawSchemaTest extends BaseTest {
  it should "create a sample record" in {
    val actual   = RawSchema.create("a", "var name", "2", 10)
    val expected = RawSchema("a", "var name", "2", new Timestamp(10), "1970-01-01")
    assert(actual == expected)
  }

}
