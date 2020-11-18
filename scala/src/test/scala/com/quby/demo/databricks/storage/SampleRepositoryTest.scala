package com.quby.demo.databricks.storage

import com.quby.demo.databricks.BaseTest
import com.quby.demo.databricks.schema.ElectricityPowerSchema
import org.joda.time.DateTime

class SampleRepositoryTest extends BaseTest {

  val randomSeed = 42

  it should "generate range for a day" in {
    val dateFrom = "2020-05-17"
    val dateTo   = "2020-05-18"

    val repository = new SampleRepository(60)

    val result = repository.range(dateFrom, dateTo)
    assert(result.length === 2 * 24)
  }

  it should "generate range for electricity power for a day" in {
    val dateFrom = "2020-05-17"
    val dateTo   = "2020-05-17"
    val rand     = new scala.util.Random(randomSeed)

    print(DateTime.parse("2020-05-17T00:00:00.0").getMillis)
    val expected = Seq(
      ElectricityPowerSchema.create("user-1", rand.nextInt(250), "2020-05-17 00:00:00.0"),
      ElectricityPowerSchema.create("user-2", rand.nextInt(600), "2020-05-17 00:00:00.0"),
      ElectricityPowerSchema.create("user-1", rand.nextInt(250), "2020-05-17 12:00:00.0"),
      ElectricityPowerSchema.create("user-2", rand.nextInt(600), "2020-05-17 12:00:00.0")
    )

    val repository = new SampleRepository(12 * 60, randomSeed)

    val result = repository.electricityPower(dateFrom, dateTo).collect()

    assert(result.toSet === expected.toSet)
  }

  it should "generate data for a day" in {
    val dateFrom = "2020-05-17"
    val dateTo   = "2020-05-17"

    val repository = new SampleRepository(60)

    val result = repository.raw(dateFrom, dateTo).collect()

    assert(result.length === 24 * 5)
  }
}
