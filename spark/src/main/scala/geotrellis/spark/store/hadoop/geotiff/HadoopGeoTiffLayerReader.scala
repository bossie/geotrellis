/*
 * Copyright 2018 Azavea
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

package geotrellis.spark.store.hadoop.geotiff

import geotrellis.layer.ZoomedLayoutScheme
import geotrellis.raster.resample.{NearestNeighbor, ResampleMethod}
import geotrellis.raster.io.geotiff.{AutoHigherResolution, OverviewStrategy}
import geotrellis.store.util.BlockingThreadPool
import geotrellis.store.hadoop.cog.byteReader
import geotrellis.util.ByteReader
import geotrellis.util.annotations.experimental

import org.apache.hadoop.conf.Configuration
import java.net.URI

import scala.concurrent.ExecutionContext

/**
  * @define experimental <span class="badge badge-red" style="float: right;">EXPERIMENTAL</span>@experimental
  */
@experimental class HadoopGeoTiffLayerReader[M[T] <: Traversable[T]](
  val attributeStore: AttributeStore[M, GeoTiffMetadata],
  val layoutScheme: ZoomedLayoutScheme,
  val resampleMethod: ResampleMethod = NearestNeighbor,
  val strategy: OverviewStrategy = AutoHigherResolution,
  val conf: Configuration = new Configuration,
  executionContext: => ExecutionContext = BlockingThreadPool.executionContext
) extends GeoTiffLayerReader[M] {
  implicit val ec: ExecutionContext = executionContext

  implicit def getByteReader(uri: URI): ByteReader = byteReader(uri, conf)
}

@experimental object HadoopGeoTiffLayerReader {
  def apply[M[T] <: Traversable[T]](
    attributeStore: AttributeStore[M, GeoTiffMetadata],
    layoutScheme: ZoomedLayoutScheme,
    resampleMethod: ResampleMethod = NearestNeighbor,
    strategy: OverviewStrategy = AutoHigherResolution,
    conf: Configuration = new Configuration,
    executionContext: => ExecutionContext = BlockingThreadPool.executionContext
  ): HadoopGeoTiffLayerReader[M] =
    new HadoopGeoTiffLayerReader(attributeStore, layoutScheme, resampleMethod, strategy, conf, executionContext)
}
