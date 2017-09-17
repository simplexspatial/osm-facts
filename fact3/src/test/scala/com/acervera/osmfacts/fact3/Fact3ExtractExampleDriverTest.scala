package com.acervera.osmfacts.fact3

import org.apache.spark.SparkConf
import org.scalatest.{FunSuite, GivenWhenThen, WordSpec}

class Fact3ExtractExampleDriverTest extends WordSpec with GivenWhenThen {

  "Fact3ExtractExampleDriver" should {

    "extractWays" in {
      Given("a set of 23 blob files and a know crossed ways from fact3")

      val nodeId = 4449495566L
      val waysIds: Seq[Long] = Seq(40159014,46593843,74484349,150718676,447886765)
      val input = "assets/osm/faroe-islands"

      When("create ways with geospatial data")
      val conf = new SparkConf().setMaster("local[4]")
      val js = Fact3ExtractExampleDriver.extractWays(conf, input, nodeId, waysIds)

      println(js)

    }

  }

}
