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

package com.acervera.osmfacts.fact4

import better.files.File
import org.apache.spark.SparkConf
import org.scalatest.{GivenWhenThen, WordSpec}

class Fact4DriverTest extends WordSpec with GivenWhenThen {

  "Fact4Driver" should {
    "extract metrics" in {
      Given("a set of 23 blob files")
      val input = "assets/osm/faroe-islands"
      val output = s"/tmp/osm-facts/fact4/${System.currentTimeMillis}/report.txt"

      When("extract metrics")
      val conf = new SparkConf().setMaster("local[4]")
      val metrics = Fact4Driver.extractMetrics(conf, input)

      Then("the metrics must be right.")
      val out = File(output)
      out.parent.createDirectories()
      out.append(Fact4Driver.buildReport(metrics))

      assert(166929 == metrics.entities)
      assert(0 == metrics.errors)
      assert(153468 == metrics.nodes)
      assert(13303 == metrics.ways)
      assert(158 == metrics.relations)
      assert(1112 == metrics.intersections)

    }
  }

}
