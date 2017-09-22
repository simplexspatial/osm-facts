package com.acervera.osmfacts.fact4

import com.acervera.osm4scala.model.{OSMTypes, RelationEntity, WayEntity}
import com.acervera.osmfacts.FactsCommons
import org.apache.log4j.{Level, LogManager}
import org.apache.spark.util.LongAccumulator
import org.apache.spark.{SparkConf, SparkContext}

object Fact4Driver extends FactsCommons {

  var log = LogManager.getLogger("com.acervera.osmfacts.fact4.Fact4Driver")



  def extraNodesFromRelation(relationEntity: RelationEntity): Seq[Long] = relationEntity.relations.map(_.id)

  def extraNodesFromWay(wayEntity: WayEntity): Seq[Long] = wayEntity.nodes


  /**
    * Extract all ways.
    */
  def extractNodesIdAndUpdateCounters(path: String, bin: Array[Byte],
                                      errorCounter: LongAccumulator,
                                      nodesCounter: LongAccumulator,
                                      waysCounter: LongAccumulator,
                                      relationsCounter: LongAccumulator,
                                      entitiesCounter: LongAccumulator): Seq[(Long, Int)] =

    parseBlob(path, bin, errorCounter).flatMap(entity => {
      entitiesCounter.add(1)
      entity.osmModel match {
        case OSMTypes.Way => {
          waysCounter.add(1)
          extraNodesFromWay(entity.asInstanceOf[WayEntity]).map( (_, 1) )
        }
        case OSMTypes.Node => {
          nodesCounter.add(1)
          Seq()
        }
        case OSMTypes.Relation => {
          relationsCounter.add(1)
          Seq()
        }
        case _ => Seq()
      }
    })

  def percentage(total: Long)(partial: Long): Double = partial * 100.0 / total

  def buildReport(metrics: Metrics) = {

    def percentageFromEntities = percentage(metrics.entities)(_)

    s"""
       |Total entities: ${metrics.entities}
       |Error: ${metrics.errors}
       |Nodes: ${metrics.nodes} => ${percentageFromEntities(metrics.nodes)}% of entities
       |Ways: ${metrics.ways} => ${percentageFromEntities(metrics.ways)}% of entities
       |Relations: ${metrics.relations} => ${percentageFromEntities(metrics.relations)}% of entities
       |Intersections: ${metrics.intersections} => ${percentage(metrics.nodes)(metrics.intersections)}% of nodes
    """.stripMargin

  }

  case class Metrics(errors: Long, nodes: Long, ways: Long, relations: Long, entities: Long, intersections: Long)

  /**
    *
    * @param defaultConfig
    * @param input
    */
  def extractMetrics(defaultConfig: SparkConf, input: String): Metrics = {


    val conf = defaultConfig.setAppName("Extract metric")
    conf.set("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
    val sc = new SparkContext(conf)

    try {

      val errorCounter = sc.longAccumulator("Errors")
      val nodesCounter = sc.longAccumulator("Nodes")
      val waysCounter = sc.longAccumulator("Ways")
      val relationsCounter = sc.longAccumulator("Relations")
      val entitiesCounter = sc.longAccumulator("Entities")

      val intersectionsCounter = sc
        .binaryFiles(input)
        .flatMap { case (path, binaryBlob) => extractNodesIdAndUpdateCounters(path, binaryBlob.toArray(), errorCounter, nodesCounter, waysCounter, relationsCounter, entitiesCounter) }
        .reduceByKey(_+_)
        .filter(_._2 > 2)
        .count()

      Metrics(
        errors = errorCounter.value,
        nodes = nodesCounter.value,
        ways = waysCounter.value,
        relations = relationsCounter.value,
        entities = entitiesCounter.value,
        intersections = intersectionsCounter
      )

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

    log.info(s"Reading from ${input} log few metrics. The party has begun!!!")
    val sparkConfig = new SparkConf()
    val metrics = extractMetrics(sparkConfig, input)

    var logMetrics = LogManager.getLogger("com.acervera.osmfacts.fact4.Fact4Driver.metrics")
    logMetrics.setLevel(Level.ALL)
    logMetrics.info(buildReport(metrics))
  }

}
