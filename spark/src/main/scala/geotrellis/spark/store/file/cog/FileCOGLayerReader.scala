/*
 * Copyright 2016 Azavea
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

package geotrellis.spark.store.file.cog

import geotrellis.layer._
import geotrellis.raster._
import geotrellis.raster.io.geotiff.reader.GeoTiffReader
import geotrellis.store._
import geotrellis.store.util._
import geotrellis.store.cog.{Extension, ZoomRange}
import geotrellis.store.file.{FileAttributeStore, FileLayerHeader, KeyPathGenerator}
import geotrellis.store.file.cog.byteReader
import geotrellis.spark.store.cog._
import geotrellis.util._

import com.typesafe.scalalogging.LazyLogging
import org.apache.spark.SparkContext
import _root_.io.circe._

import java.net.URI
import java.io.File

import scala.concurrent.ExecutionContext
import scala.reflect.ClassTag

/**
 * Handles reading raster RDDs and their metadata from S3.
 *
 * @param attributeStore  AttributeStore that contains metadata for corresponding LayerId
 */
class FileCOGLayerReader(
  val attributeStore: AttributeStore,
  val catalogPath: String,
  executionContext: => ExecutionContext = BlockingThreadPool.executionContext
)(@transient implicit val sc: SparkContext) extends COGLayerReader[LayerId] with LazyLogging {

  @transient implicit lazy val ec: ExecutionContext = executionContext

  val defaultNumPartitions: Int = sc.defaultParallelism

  implicit def getByteReader(uri: URI): ByteReader = byteReader(uri)

  def pathExists(path: String): Boolean =
    new File(path).isFile

  def fullPath(path: String): URI =
    new URI(s"file://$path")

  def getHeader(id: LayerId): FileLayerHeader =
    try {
      attributeStore.readHeader[FileLayerHeader](LayerId(id.name, 0))
    } catch {
      case e: AttributeNotFoundError => throw new LayerNotFoundError(id).initCause(e)
    }

  def produceGetKeyPath(id: LayerId): (ZoomRange, Int) => BigInt => String = {
    val header = getHeader(id)
    (zoomRange: ZoomRange, maxWidth: Int) =>
      KeyPathGenerator(header.path, s"${id.name}/${zoomRange.slug}", maxWidth) andThen (_ ++ s".$Extension")
  }

  def read[
    K: SpatialComponent: Boundable: Decoder: ClassTag,
    V <: CellGrid[Int]: GeoTiffReader: ClassTag
  ](id: LayerId, tileQuery: LayerQuery[K, TileLayerMetadata[K]], numPartitions: Int) =
    baseReadAllBands[K, V](
      id              = id,
      tileQuery       = tileQuery,
      numPartitions   = numPartitions
    )

  def readSubsetBands[
    K: SpatialComponent: Boundable: Decoder: ClassTag
  ](
    id: LayerId,
    targetBands: Seq[Int],
    rasterQuery: LayerQuery[K, TileLayerMetadata[K]],
    numPartitions: Int
  ) =
    baseReadSubsetBands[K](id, targetBands, rasterQuery, numPartitions)
}

object FileCOGLayerReader {
  def apply(attributeStore: AttributeStore, catalogPath: String)(implicit sc: SparkContext): FileCOGLayerReader =
    new FileCOGLayerReader(attributeStore, catalogPath)

  def apply(catalogPath: String)(implicit sc: SparkContext): FileCOGLayerReader =
    apply(new FileAttributeStore(catalogPath), catalogPath)

  def apply(attributeStore: FileAttributeStore)(implicit sc: SparkContext): FileCOGLayerReader =
    apply(attributeStore, attributeStore.catalogPath)
}
