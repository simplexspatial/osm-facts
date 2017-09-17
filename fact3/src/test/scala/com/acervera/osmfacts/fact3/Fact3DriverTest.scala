package com.acervera.osmfacts.fact3

import org.apache.spark.SparkConf
import org.scalatest.{GivenWhenThen, WordSpec}
import better.files._
import com.acervera.osmfacts.fact3.Fact3Driver._

class Fact3DriverTest extends WordSpec with GivenWhenThen {



  "Fact3DriverTest" should {

    "searchVerticesBetweenTheEnds" in {

      Given("a set of 23 blob files")
      val input = "assets/osm/faroe-islands"
      val output = s"/tmp/osm-facts/fact3/${System.currentTimeMillis}"

      When("search shared non extremed nodes")
      val conf = new SparkConf().setMaster("local[4]")
      Fact3Driver.searchVerticesBetweenTheEnds(conf, input, output)

      Then("realise that there are shared nodes that are not ate the ends of the way.")
      val lines = File(output).glob("**/part-*").map(_.lines)
      assert(lines.length > 0)

    }

    "areAllExtremes" should {
      "must detect non extreme" in assert(!areAllExtremes(Iterable(true, false, false, true)))
      "must detect all extremes" in assert(areAllExtremes(Iterable(true, true, true, true)))
    }

  }

}
