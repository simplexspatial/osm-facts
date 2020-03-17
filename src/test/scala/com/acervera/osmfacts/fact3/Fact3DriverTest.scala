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
      "must detect non extreme" in assert(!areAllAtTheEnds(Iterable((1, true), (2, false), (3, false), (4, true))))
      "must detect all extremes" in assert(areAllAtTheEnds(Iterable((1, true), (2, true), (3, true), (4, true))))
    }

  }

}
