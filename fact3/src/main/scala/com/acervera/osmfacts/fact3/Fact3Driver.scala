package com.acervera.osmfacts.fact3

import com.acervera.osm4scala.EntityIterator
import com.acervera.osm4scala.model.{OSMEntity, OSMTypes, WayEntity}
import org.apache.log4j.LogManager
import org.apache.spark.util.LongAccumulator
import org.apache.spark.{SparkConf, SparkContext}
import org.openstreetmap.osmosis.osmbinary.fileformat.Blob

import scala.util.{Failure, Success, Try}

object Fact3Driver {

  var log = LogManager.getLogger("com.acervera.osmfacts.fact3.Fact3Driver")

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
      case Failure(ex) => {
        errorCounter.add(1)
        log.error(s"Error reading blob file ${path}", ex)
        Seq()
      }
    }

  /**
    * Extract all nodes from the way, tagging if it is in the extreme or is isn't.
    *
    * @param way
    * @return (nodeId, true if it's in the extreme)
    */
  def tagNodes(way: WayEntity): Seq[(Long, Seq[Boolean])] =
    way.nodes.zipWithIndex.map { case(node, idx) => (node, Seq(idx==0 || idx==way.nodes.length-1))}

  /**
    * Check that all are in the extreme.
    *
    *
    * @param extreme
    * @return
    */
  def areAllExtremes(extreme:Iterable[Boolean]): Boolean = extreme.forall(v => v)

  /**
    * Extract all ways.
    */
  def extractWays(path: String, bin: Array[Byte], errorCounter: LongAccumulator): Seq[WayEntity] =
    parseBlob(path, bin, errorCounter).filter(_.osmModel == OSMTypes.Way).map(_.asInstanceOf[WayEntity])


  /**
    * Extract all nodes that are connections between ways and are between the ends of the way.
    *
    * @param defaultConfig
    * @param input
    * @param output
    */
  def searchVerticesBetweenTheEnds(defaultConfig: SparkConf, input: String, output: String) = {

    val conf = defaultConfig.setAppName("Check connections in extremes")
    conf.set("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
    val sc = new SparkContext(conf)
    try {

      val errorCounter = sc.longAccumulator

      sc
        .binaryFiles(input)
        .flatMap { case (path, binaryBlob) => extractWays(path, binaryBlob.toArray(), errorCounter) }
        .flatMap(tagNodes)
        .reduceByKey(_ ++ _) // aggregate by node id.
        .filter(_._2.size > 2) // Remove nodes that are not shared vertices.
        .filter{ case(_, extremes) => ! areAllExtremes(extremes) } // Keep nodes with connection between the ends.
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
