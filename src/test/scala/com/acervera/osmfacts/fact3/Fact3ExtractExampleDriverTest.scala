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

package com.acervera.osmfacts.fact3

import org.apache.spark.SparkConf
import org.scalatest.{FunSuite, GivenWhenThen, WordSpec}

class Fact3ExtractExampleDriverTest extends WordSpec with GivenWhenThen {

  "Fact3ExtractExampleDriver" should {

    "extractWays" in {
      Given("a set of 23 blob files and a know crossed ways from fact3")

      val nodeId = 4449495566L
      val waysIds: Seq[Long] = Seq(40159014, 46593843, 74484349, 150718676, 447886765)
      val input = "assets/osm/faroe-islands"

      When("create ways with geospatial data")
      val conf = new SparkConf().setMaster("local[4]")
      val js = Fact3ExtractExampleDriver.extractWays(conf, input, nodeId, waysIds)

      println(js)

    }

  }

}
