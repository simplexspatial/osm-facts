package com.acervera.osmfacts.fact3

import com.acervera.osm4scala.model.{NodeEntity, OSMTypes, WayEntity}
import com.acervera.osmfacts.FactsCommons
import org.apache.spark.{SparkConf, SparkContext}

/**
  * Passing a node and ways, extract all ways' coordinates and the node coordinate preparing it for the javascript demo.
  *
  * Used only from the integration test.
  */
object Fact3ExtractExampleDriver extends FactsCommons {

  case class LatLng(lat: Double, lng: Double) {
    def toLatLongString = s"[$lat,$lng]"
  }

  def extractWays(defaultConfig: SparkConf, input: String, nodeIntersectionId: Long, wayIds: Seq[Long]) = {

    val conf = defaultConfig.setAppName("Check connections in extremes")
    conf.set("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
    val sc = new SparkContext(conf)
    try {

      val errorCounter = sc.longAccumulator

      // Process entities to cache only necessary data.
      val entities = sc.binaryFiles(input)
        .flatMap { case (path, binaryBlob) => parseBlob(path, binaryBlob.toArray(), errorCounter) }
        .filter( entity => entity.osmModel == OSMTypes.Way || entity.osmModel == OSMTypes.Node )
        .flatMap{
          case entity if entity.osmModel == OSMTypes.Way && wayIds.contains(entity.id) => {
            val way = entity.asInstanceOf[WayEntity]
            Some(Left( (way.id, way.nodes) )) // Ways with nodes.
          }
          case entity if entity.osmModel == OSMTypes.Node => {
            val node = entity.asInstanceOf[NodeEntity]
            Some(Right( (node.id, LatLng(node.latitude, node.longitude)) )) // Nodes with Coords.
          }
          case _ => None
        }
        .cache()

      // WARNING!!!! We can do the follow because we know that ways and nodes related with ways data fit in memory.

      val ways: Array[(Long, Seq[Long])] = entities.flatMap{
        case Left(way) => Some(way)
        case _ => None
      }.collect()

      val nodeIds: Seq[Long] = ways.flatMap(_._2)

      val nodes: Map[Long,LatLng] = entities.flatMap{
        case Right(node) if(nodeIds.contains(node._1)) => Some(node)
        case _ => None
      }.collect().toMap

      // Generate Javascript
      val intersectionPoint = nodes(nodeIntersectionId).toLatLongString
      val waysJs = ways.map( way => way._2.map(nodes(_).toLatLongString) ).map(_.mkString("[",",","]"))

      val javascript =
        s"""
           |var fact3IntersectionPoint = ${intersectionPoint}
           |var fact3Ways = ${waysJs.mkString("[\n   ",",\n   ","\n]")}
    """.stripMargin

      javascript

    } finally {
      if (!sc.isStopped) sc.stop()
    }
  }

}
