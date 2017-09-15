package com.acervera.osmfacts.fact2

import org.apache.spark.SparkConf
import org.scalatest.{GivenWhenThen, WordSpec}

class Fact2DriverTest extends WordSpec with GivenWhenThen {

  "Fact2DriverTest" should {

    "searchNonUniqueIds" in {

      Given("a set of 23 blob files")
      val input = "assets/osm/faroe-islands"

      When("search duplicates")
      val conf = new SparkConf().setMaster("local[4]")
      val duplicatesCount = Fact2Driver.searchNonUniqueIds(conf, input)

      Then("return 0 because there are no duplicates.")
      assert(duplicatesCount == 0)

    }

  }

}
