package com.acervera.osmfacts.fact1

import com.acervera.osmfacts.CommonStuffTest
import com.acervera.osmfacts.fact1.Fact1Driver.{BBox, Point}
import org.apache.spark.SparkConf
import org.scalatest.{GivenWhenThen, WordSpec}

class Fact1DriverTest extends WordSpec with GivenWhenThen with CommonStuffTest {

  val outRoot = s"${outputTemporalRoot}/fact1"

  "Fact1DriverTest" should {

    "calculateBoundingAreas" in {

      Given("a set of 23 blob files")
      val input = "fact1/src/test/resources/faroe-islands"

      When("extract bounding data")
      val defaultConfig = new SparkConf().setMaster("local[4]")

      Then("return 20 elements, because remove HeaderBlock and keep only OSMData with nodes.")
      val boundingData = Fact1Driver.calculateBoundingAreas(defaultConfig, input)
      assert(boundingData.length == 20)

    }

    "updateBBox few point in different areas" in {

      val bbox = Some(BBox(Point(-2,-1), Point(1,3)))

      assert(Fact1Driver.updateBBox(bbox, Point(-1,1)) == bbox.get, "point inside must noy change the bbox")
      assert(Fact1Driver.updateBBox(bbox, Point(-1,5)) == BBox(Point(-2,-1), Point(1,5)), "point with upper lat")
      assert(Fact1Driver.updateBBox(bbox, Point(3,5)) == BBox(Point(-2,-1), Point(3,5)), "point with upper lat and lng")
      assert(Fact1Driver.updateBBox(bbox, Point(3,1)) == BBox(Point(-2,-1), Point(3,3)), "point with upper lng")
      assert(Fact1Driver.updateBBox(bbox, Point(3,-3)) == BBox(Point(-2,-3), Point(3,3)), "point with lower lat and upper lng")
      assert(Fact1Driver.updateBBox(bbox, Point(-1,-3)) == BBox(Point(-2,-3), Point(1,3)), "point with lower lat")
      assert(Fact1Driver.updateBBox(bbox, Point(-4,-2)) == BBox(Point(-4,-2), Point(1,3)), "point with lower lat and lng")
      assert(Fact1Driver.updateBBox(bbox, Point(-4,2)) == BBox(Point(-4,-1), Point(1,3)), "point with lower lng")
      assert(Fact1Driver.updateBBox(bbox, Point(-4,4)) == BBox(Point(-4,-1), Point(1,4)), "point with upper lat and lower lng")

    }

  }

}
