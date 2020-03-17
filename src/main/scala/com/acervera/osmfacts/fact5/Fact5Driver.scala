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

import com.acervera.osm4scala.model.{NodeEntity, WayEntity}
import com.acervera.osmfacts.FactsCommons
import org.apache.log4j.{Level, LogManager}
import org.apache.spark.util.LongAccumulator
import org.apache.spark.{SparkConf, SparkContext}

object Fact5Driver extends FactsCommons {

  var log = LogManager.getLogger("com.acervera.osmfacts.fact5.Fact5Driver")

  /**
    *
    * @param args Input , Output
    */
  def main(args: Array[String]): Unit = {
    val input = args(0)
    val output = args(1)

    log.info(s"Searching not found ids from [${input}]. The party has begun!!!")
    val sparkConfig = new SparkConf()
    val metrics = searchNotFoundIds(sparkConfig, input, output)

    val logMetrics = LogManager.getLogger("com.acervera.osmfacts.fact5.Fact5Driver.metrics")
    logMetrics.setLevel(Level.ALL)
    logMetrics.info(buildReport(metrics))

  }

  case class Metrics(errors: Long, nodes: Long, nodesUsedInWays: Long, nodesNotFound: Long)

  def buildReport(metrics: Metrics): String = {

    s"""
       |Total nodes ids: ${metrics.nodes}
       |Error: ${metrics.errors}
       |Nodes used in ways: ${metrics.nodesUsedInWays}
       |Nodes used in ways but not found: ${metrics.nodesNotFound}
    """.stripMargin

  }

  def extractIdsFromNodes(path: String, blob: Array[Byte], errorCounter: LongAccumulator): Seq[Long] =
    parseBlob(path, blob, errorCounter).flatMap {
      case node: NodeEntity => Some(node.id)
      case _                => None
    }

  def extractIdsFromWays(path: String, blob: Array[Byte], errorCounter: LongAccumulator): Seq[Long] =
    parseBlob(path, blob, errorCounter).flatMap {
      case way: WayEntity => way.nodes
      case _              => Seq.empty
    }

  def searchNotFoundIds(defaultConfig: SparkConf, input: String, output: String): Metrics = {

    val conf = defaultConfig.setAppName("Search for not found ids")
    conf.set("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
    val sc = new SparkContext(conf)

    try {

      val errorCounter = sc.longAccumulator

      val nodeIds = sc
        .binaryFiles(input)
        .flatMap { case (path, binaryBlob) => extractIdsFromNodes(path, binaryBlob.toArray(), errorCounter) }
        .distinct()

      val nodesUsedInWaysIds = sc
        .binaryFiles(input)
        .flatMap { case (path, binaryBlob) => extractIdsFromWays(path, binaryBlob.toArray(), errorCounter) }
        .distinct()

      val nodesNotFound = nodesUsedInWaysIds.subtract(nodeIds).persist()

      nodesNotFound.saveAsTextFile(output)

      Metrics(
        errorCounter.count,
        nodeIds.count(),
        nodesUsedInWaysIds.count(),
        nodesNotFound.count()
      )

    } finally {
      if (!sc.isStopped) sc.stop()
    }
  }

}
