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

package geotrellis.raster.split

import geotrellis.raster._

import spire.syntax.cfor._

import Split.Options

abstract class RasterSplitMethods[T <: CellGrid[Int]: (? => SplitMethods[T])] extends SplitMethods[Raster[T]] {
  def split(tileLayout: TileLayout, options: Options): Seq[Raster[T]] =
    self.rasterExtent.split(tileLayout, options)
      .zip(self.tile.split(tileLayout, options))
      .map { case (re, tile) => Raster(tile, re.extent) }
}

trait SinglebandRasterSplitMethods extends RasterSplitMethods[Tile]
trait MultibandRasterSplitMethods extends RasterSplitMethods[MultibandTile]
