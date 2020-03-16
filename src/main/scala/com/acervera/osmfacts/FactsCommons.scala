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
      case Failure(ex) => {
        errorCounter.add(1)
        log.error(s"Error reading blob file ${path}", ex)
        Seq()
      }
    }

}
