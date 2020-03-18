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

package com.acervera.osmfacts.fact5

import com.acervera.osmfacts.fact5.Fact5Driver.Metrics
import org.apache.spark.SparkConf
import org.scalatest.{GivenWhenThen, Matchers, WordSpec}

class Fact5DriverTest extends WordSpec with GivenWhenThen with Matchers {

  "Fact5Driver" should {
    "extract metrics" in {
      Given("a set of 23 blob files")
      val input = "assets/osm/monaco"
      val output = s"/tmp/osm-facts/fact5-${System.currentTimeMillis}"

      When("search ids")
      val conf = new SparkConf().setMaster("local[4]")
      val metrics = Fact5Driver.searchNotFoundIds(conf, input, output)

      metrics shouldBe Metrics(0, 46179, 44904, 0)
    }
  }

}
