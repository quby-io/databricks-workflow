package com.quby.demo.databricks.util

import com.quby.demo.databricks.BaseTest
import org.joda.time.DateTime

class JobUtilsTest extends BaseTest {

  "getOrTodayUTC" should "return the same passed date" in {
    assert(JobUtils.getOrTodayUTC("2020-06-05") == "2020-06-05")
  }

  it should "return today UTC when passed an empty string" in {
    val todayUTC = JobUtils.getOrTodayUTC("")

    val todayUTCDate = DateTime.parse(todayUTC) // It should be a valid date
    assert(todayUTCDate.isBeforeNow) // It should be at midnight UTC of current day, which is before now
  }

  "subtractDays" should "subtract the correct number of days" in {
    assert(JobUtils.subtractDays("2020-06-05", 0) == "2020-06-05")
    assert(JobUtils.subtractDays("2020-06-05", 1) == "2020-06-04")
  }


}
