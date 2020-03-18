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
