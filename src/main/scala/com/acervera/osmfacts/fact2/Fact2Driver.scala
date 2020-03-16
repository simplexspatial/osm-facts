package com.acervera.osmfacts.fact2

import com.acervera.osmfacts.FactsCommons
import org.apache.log4j.LogManager
import org.apache.spark.util.LongAccumulator
import org.apache.spark.{SparkConf, SparkContext}

object Fact2Driver extends FactsCommons {

  var log = LogManager.getLogger("com.acervera.osmfacts.fact2.Fact2Driver")

  /**
    * FRom a Blob binary format, extract all  ids.
    *
    * @param path
    * @param bin
    * @param errors
    * @return
    */
  def extractIdsFromBlob(path: String, bin: Array[Byte], errors: LongAccumulator): Seq[(Long, Int)] =
    parseBlob(path, bin, errors).map( entity => (entity.id,1) )

  /**
    * Count duplicates.
    *
    * @param defaultConfig
    * @param input
    * @return
    */
  def searchNonUniqueIds(defaultConfig: SparkConf, input: String) = {

    val sparkConf = defaultConfig.setAppName("Search duplicates")
    sparkConf.set("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
    val sc = new SparkContext(sparkConf)

    try {
      val errorAcc = sc.longAccumulator("error_files")

      sc.binaryFiles(input)
        .flatMap{case(name, portable) => extractIdsFromBlob(name, portable.toArray(), errorAcc) }
        .reduceByKey(_ + _)
        .filter(_._2 > 1)
        .count()

    } finally {
      if (!sc.isStopped) sc.stop()
    }

  }

  /**
    * First argument is input data set.
    *
    * @param args
    */
  def main(args: Array[String]): Unit = {

    val input = args(0)

    log.info(s"Reading from ${input}. The party has begun!!!")

    val sparkConfig = new SparkConf()
    val count = searchNonUniqueIds(sparkConfig, input)

    log.info(s"Found ${count} duplicates.")

    assert(count == 0, s"Error checking duplicates. Found ${count} non unique Ids. ")

  }

}
