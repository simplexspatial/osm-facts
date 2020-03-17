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

package com.acervera.osmfacts

import com.acervera.osm4scala.EntityIterator
import com.acervera.osm4scala.model.OSMEntity
import com.acervera.osmfacts.fact3.Fact3Driver.log
import org.apache.spark.util.LongAccumulator
import org.openstreetmap.osmosis.osmbinary.fileformat.Blob

import scala.util.{Failure, Success, Try}

trait FactsCommons {

  /**
    * Transform the file into a blob
    *
    * @param path
    * @param bin
    * @param errorCounter
    * @return
    */
  def parseBlob(path: String, bin: Array[Byte], errorCounter: LongAccumulator): Seq[OSMEntity] =
    Try(EntityIterator.fromBlob(Blob.parseFrom(bin)).toSeq) match {
      case Success(entities) => entities
      case Failure(ex) =>
        errorCounter.add(1)
        log.error(s"Error reading blob file ${path}", ex)
        Seq()
    }

}
