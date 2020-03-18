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

package com.acervera.osmfacts.fact6

import java.io.FileInputStream

import com.acervera.osm4scala.EntityIterator
import com.acervera.osm4scala.model.{NodeEntity, OSMEntity, WayEntity}
import io.tmos.arm.ArmMethods._
import org.roaringbitmap.longlong.Roaring64NavigableMap
import org.slf4j.LoggerFactory

import scala.annotation.tailrec

object Fact6Driver {

  var log = LoggerFactory.getLogger("com.acervera.osmfacts.fact6.Fact6Driver")

  /**
    *
    * @param args Input
    */
  def main(args: Array[String]): Unit = {
    val input = args(0)

    log.info(s"Checking presence order of ids from [$input]. The party has begun!!!")
    searchFirstNotFound(args(0)) match {
      case Some((entity, id)) => println(s"Node $id not found at the moment, but used in the way ${entity.id}")
      case None               => println("All nodes found before to be used.")
    }
  }

  @tailrec
  private def rec(ids: Roaring64NavigableMap, entities: EntityIterator): Option[(OSMEntity, Long)] = {

    if (entities.hasNext) {
      entities.next() match {
        case osmEntity: NodeEntity =>
          ids.add(osmEntity.id)
          rec(ids, entities)
        case osmEntity: WayEntity =>
          osmEntity.nodes.find(!ids.contains(_)) match {
            case Some(id) => Some((osmEntity, id))
            case _        => rec(ids, entities)
          }
        case _ => rec(ids, entities)
      }
    } else {
      None
    }
  }

  def searchFirstNotFound(pbfFilePath: String): Option[(OSMEntity, Long)] =
    for {
      pbfIS <- manage(new FileInputStream(pbfFilePath))
    } yield {
      rec(new Roaring64NavigableMap(), EntityIterator.fromPbf(pbfIS))
    }

}
