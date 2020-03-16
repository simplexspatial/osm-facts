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

import com.acervera.osm4scala.model.{OSMTypes, WayEntity}
import com.acervera.osmfacts.FactsCommons
import org.apache.log4j.LogManager
import org.apache.spark.util.LongAccumulator
import org.apache.spark.{SparkConf, SparkContext}

object Fact3Driver extends FactsCommons {

  var log = LogManager.getLogger("com.acervera.osmfacts.fact3.Fact3Driver")

  /**
    * Extract all nodes from the way, tagging if it is in the extreme or is isn't.
    *
    * @param way
    * @return (nodeId, (wayId, true if it's in the extreme))
    */
  def tagNodes(way: WayEntity): Seq[(Long, Seq[(Long, Boolean)])] =
    way.nodes.zipWithIndex.map { case (node, idx) => (node, Seq((way.id, idx == 0 || idx == way.nodes.length - 1))) }

  /**
    * Check that all are in the extreme.
    *
    *
    * @param ends
    * @return
    */
  def areAllAtTheEnds(ends: Iterable[(Long, Boolean)]): Boolean = ends.forall(v => v._2)

  /**
    * Extract all ways.
    */
  def extractWays(path: String, bin: Array[Byte], errorCounter: LongAccumulator): Seq[WayEntity] =
    parseBlob(path, bin, errorCounter).filter(_.osmModel == OSMTypes.Way).map(_.asInstanceOf[WayEntity])

  /**
    * Extract all nodes that are connections between ways and are between the ends of the way.
    * The output is going to be a CSV where first is the nodeId shared and the rest the list of ways.
    *
    * @param defaultConfig
    * @param input
    * @param output
    */
  def searchVerticesBetweenTheEnds(defaultConfig: SparkConf, input: String, output: String): Unit = {

    val conf = defaultConfig.setAppName("Check connections in extremes")
    conf.set("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
    val sc = new SparkContext(conf)

    try {

      val errorCounter = sc.longAccumulator

      val rddResult = sc
        .binaryFiles(input)
        .flatMap { case (path, binaryBlob) => extractWays(path, binaryBlob.toArray(), errorCounter) }
        .flatMap(tagNodes)
        .reduceByKey(_ ++ _) // aggregate by node id.
        .filter(_._2.size > 2) // Remove nodes that are not shared vertices.
        .filter { case (_, theEnds) => !areAllAtTheEnds(theEnds) } // Keep nodes with connection between the ends.
        .map(intersections => (intersections._1 +: intersections._2.map(_._1)).mkString(","))
        .saveAsTextFile(output)

    } finally {
      if (!sc.isStopped) sc.stop()
    }
  }

  /**
    *
    * @param args Input , Output
    */
  def main(args: Array[String]): Unit = {
    val input = args(0)
    val output = args(1)

    log.info(s"Reading from ${input} and writing to ${output}. The party has begun!!!")

    val sparkConfig = new SparkConf()
    searchVerticesBetweenTheEnds(sparkConfig, input, output)
  }

}
