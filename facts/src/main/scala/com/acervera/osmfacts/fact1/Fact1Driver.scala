package com.acervera.osmfacts.fact1

import better.files._
import com.acervera.osm4scala.EntityIterator
import com.acervera.osm4scala.model._
import com.acervera.osmfacts.FactsCommons
import com.typesafe.config.{Config, ConfigFactory}
import net.ceedubs.ficus.Ficus._
import org.apache.log4j.LogManager
import org.apache.spark.util.CollectionAccumulator
import org.apache.spark.{SparkConf, SparkContext}
import org.openstreetmap.osmosis.osmbinary.fileformat.Blob

import scala.util.{Failure, Success, Try}

// TODO: REfactor to use FactsCommons
object Fact1Driver extends FactsCommons {

  var log = LogManager.getLogger("com.acervera.osmfacts.fact1.Fact1Driver")

  case class BBox(lowLeft:Point, upRight:Point) {
    def toCoords = f"[${lowLeft.lat}%1.6f,${lowLeft.lng}%1.6f,${upRight.lat}%1.6f,${upRight.lng}%1.6f]"
  }
  case class Point(lng:Double, lat:Double)

  /**
    * Update the BBox in function of the location of the point.
    *
    * @param old Old BBox
    * @param point Point that must be inside of the new bbox
    * @return New updated BBox
    */
  def updateBBox(old: Option[BBox], point: Point): BBox = old match {
    case None => BBox(point, point)
    case Some(prev) => BBox(
      upRight = Point(
        lng = if (point.lng > prev.upRight.lng) point.lng else prev.upRight.lng,
        lat = if (point.lat > prev.upRight.lat) point.lat else prev.upRight.lat
      ),
      lowLeft = Point(
        lng = if (point.lng < prev.lowLeft.lng) point.lng else prev.lowLeft.lng,
        lat = if (point.lat < prev.lowLeft.lat) point.lat else prev.lowLeft.lat
      )
    )
  }

  /**
    * FRom a Blob binary format, extract the bounding area and calculate useful metrics.
    *
    * @param path
    * @param bin
    * @param errors
    * @return
    */
  def extractBoundingDataFromBlob(path: String, bin: Array[Byte], errors: CollectionAccumulator[String]): Option[BBox] =
    Try(EntityIterator.fromBlob(Blob.parseFrom(bin)).toSeq) match {
      case Success(entities) => calculateBoundingAreas(entities)
      case Failure(ex) => {
        errors.add(path)
        log.error(s"Error reading blob file ${path}", ex)
        None
      }
    }

  /**
    * Calculate the BBox for a sequence of Entities.
    *
    * @param entities
    * @return
    */
  def calculateBoundingAreas(entities: Seq[OSMEntity]): Option[BBox] =
    entities.foldLeft(Option.empty[BBox]){ case(prev, entity) => {
      entity.osmModel match {
        case OSMTypes.Node => {
          val node = entity.asInstanceOf[NodeEntity]
          Some(updateBBox(prev,Point(node.longitude, node.latitude)))
        }
        case _ => prev
      }
    }}

  /**
    * Generate a list of BBoxes for the full data set.
    *
    * @param defaultConfig
    * @param input
    * @return
    */
  def calculateBoundingAreas(defaultConfig: SparkConf, input: String) = {

    val sparkConf = defaultConfig.setAppName("Bounding areas")
    sparkConf.set("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
    val sc = new SparkContext(sparkConf)

    try {
      val errorAcc = sc.collectionAccumulator[String]("error_files")

      sc.binaryFiles(input)
        .flatMap{case(name, portable) => extractBoundingDataFromBlob(name, portable.toArray(), errorAcc) }
        .collect()
    } finally {
      if (!sc.isStopped) sc.stop()
    }

  }

  /**
    * Two typesafe config style parameters:
    *
    * osm-facts.input -> Where is the data set: OSM data blocks.
    * osm-facts.local-file-js-bounding -> Where is going to be stored the generated javascript with bounding boxes. It must to be a local file path in the Driver.
    *
    * @param args
    */
  def main(args: Array[String]): Unit = {

    log.info("The party has begun!!!")

    val appConfig: Config = ConfigFactory.load()
    val input = appConfig.as[String]("osm-facts.input")
    val output = appConfig.as[String]("osm-facts.local-file-js-bounding")


    val sparkConfig = new SparkConf()
    val boundingBoxes = calculateBoundingAreas(sparkConfig, input)

    val js = "var bboxes = " + boundingBoxes.map(_.toCoords).mkString("[",",","]")
    val outFile = File(output)
    outFile.parent.createDirectories()
    outFile.overwrite(js)

  }

}
