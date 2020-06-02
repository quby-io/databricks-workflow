package com.quby.demo.databricks.transformations

import com.quby.demo.databricks.BaseTest
import com.quby.demo.databricks.schema.{RawSchema, ElectricityPowerSchema}

class ElectricityPowerTest extends BaseTest {

  import spark.implicits._

  it should "extract power from raw" in {
    val raw = Seq(
      RawSchema.create("user-1", "power_w", "1", 1)
    ).toDS()

    val expected = Seq(
      ElectricityPowerSchema.create("user-1", 1, 1)
    )

    val result = ElectricityPower.transform(raw).collect()

    assert(result.length === expected.length)
    assert(result.toSet === expected.toSet)
  }

  it should "ignore other signals" in {
    val raw = Seq(
      RawSchema.create("user-1", "temperature_C", "22", 1),
      RawSchema.create("user-1", "gas_m3", "0.1", 1)
    ).toDS()

    val expected = Seq[ElectricityPowerSchema]()

    val result = ElectricityPower.transform(raw).collect()

    assert(result.length === expected.length)
    assert(result.toSet === expected.toSet)
  }

  it should "remove out of range values" in {
    val raw = Seq(
      RawSchema.create("user-1", "power_w", "-1", 1),
      RawSchema.create("user-1", "power_w", "0", 2),
      RawSchema.create("user-1", "power_w", "1", 3),
      RawSchema.create("user-1", "power_w", "50000", 4),
      RawSchema.create("user-1", "power_w", "50001", 5)
    ).toDS()

    val expected = Seq[ElectricityPowerSchema](
      ElectricityPowerSchema.create("user-1", 0, 2),
      ElectricityPowerSchema.create("user-1", 1, 3),
      ElectricityPowerSchema.create("user-1", 50000, 4)
    )

    val result = ElectricityPower.transform(raw).collect()

    assert(result.length === expected.length)
    assert(result.toSet === expected.toSet)
  }

  it should "remove invalid values" in {
    val raw = Seq(
      RawSchema.create("user-1", "power_w", "X", 1),
      RawSchema.create("user-1", "power_w", "", 2),
      RawSchema.create("user-1", "power_w", null, 3)
    ).toDS()

    val expected = Seq[ElectricityPowerSchema]()

    val result = ElectricityPower.transform(raw).collect()

    assert(result.length === expected.length)
    assert(result.toSet === expected.toSet)
  }

}
