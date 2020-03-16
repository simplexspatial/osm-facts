/*
 * Copyright 2020 Angel Cervera Claudio
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.acervera.osmfacts.fact1

import com.acervera.osmfacts.fact1.Fact1Driver.{BBox, Point}
import org.apache.spark.SparkConf
import org.scalatest.{GivenWhenThen, WordSpec}

class Fact1DriverTest extends WordSpec with GivenWhenThen {

  "Fact1DriverTest" should {

    "calculateBoundingAreas" in {

      Given("a set of 23 blob files")
      val input = "assets/osm/faroe-islands"

      When("extract bounding data")
      val defaultConfig = new SparkConf().setMaster("local[4]")
      val boundingData = Fact1Driver.calculateBoundingAreas(defaultConfig, input)

      Then("return 20 elements, because remove HeaderBlock and keep only OSMData with nodes.")
      assert(boundingData.length == 20)

    }

    "updateBBox few point in different areas" in {

      val bbox = Some(BBox(Point(-2, -1), Point(1, 3)))

      assert(Fact1Driver.updateBBox(bbox, Point(-1, 1)) == bbox.get, "point inside must noy change the bbox")
      assert(Fact1Driver.updateBBox(bbox, Point(-1, 5)) == BBox(Point(-2, -1), Point(1, 5)), "point with upper lat")
      assert(
        Fact1Driver.updateBBox(bbox, Point(3, 5)) == BBox(Point(-2, -1), Point(3, 5)),
        "point with upper lat and lng"
      )
      assert(Fact1Driver.updateBBox(bbox, Point(3, 1)) == BBox(Point(-2, -1), Point(3, 3)), "point with upper lng")
      assert(
        Fact1Driver.updateBBox(bbox, Point(3, -3)) == BBox(Point(-2, -3), Point(3, 3)),
        "point with lower lat and upper lng"
      )
      assert(Fact1Driver.updateBBox(bbox, Point(-1, -3)) == BBox(Point(-2, -3), Point(1, 3)), "point with lower lat")
      assert(
        Fact1Driver.updateBBox(bbox, Point(-4, -2)) == BBox(Point(-4, -2), Point(1, 3)),
        "point with lower lat and lng"
      )
      assert(Fact1Driver.updateBBox(bbox, Point(-4, 2)) == BBox(Point(-4, -1), Point(1, 3)), "point with lower lng")
      assert(
        Fact1Driver.updateBBox(bbox, Point(-4, 4)) == BBox(Point(-4, -1), Point(1, 4)),
        "point with upper lat and lower lng"
      )

    }

  }

}
