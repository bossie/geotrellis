/*
 * Copyright 2019 Azavea
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

package geotrellis.raster

import geotrellis.vector.{Extent, Point}
import geotrellis.raster.testkit._

import scala.math.{min, max}

import org.scalatest._

class GridExtentSpec extends FunSpec with Matchers {
  // Other relevant tests are under RasterExtentSpec
  describe("A GridExtent object") {
    val e1 = Extent(0.0, 0.0, 1.0, 1.0)
    val e2 = Extent(0.0, 0.0, 20.0, 20.0)

    val g1 = GridExtent[Int](e1, CellSize(1.0, 1.0))
    val g2 = GridExtent[Int](e2, CellSize(1.0, 1.0))
    val g3 = g1
    val g4 = GridExtent[Long](e1, CellSize(1.0, 1.0))

    it("should stringify") {
      val s = g1.toString
      info(s)
    }

    it("should equal for Int and Long columns") {
      (g1 == g3) shouldBe true
    }

    it("should throw when overflowing from Long to Int") {
      an [GeoAttrsError] should be thrownBy {
        new GridExtent[Long](e1, Long.MaxValue, Long.MaxValue).toGridType[Int]
      }

      an [GeoAttrsError] should be thrownBy {
        new GridExtent[Long](e1, Int.MaxValue.toLong+1, Int.MaxValue.toLong+1).toGridType[Int]
      }
    }

    val g = GridExtent[Int](Extent(10.0, 15.0, 90.0, 95.0), CellSize(2.0, 2.0))

    it("should allow aligned grid creation") {
      def isWhole(x: Double): Boolean = (x.round - x).abs < geotrellis.util.Constants.FLOAT_EPSILON

      (for (i <- (0 to 10000).toSeq) yield {
        val x0 = scala.util.Random.nextDouble * 100
        val y0 = scala.util.Random.nextDouble * 100
        val cw = scala.util.Random.nextDouble
        val ch = scala.util.Random.nextDouble
        val x1 = cw * (scala.util.Random.nextInt.abs % 20 + 1) + x0
        val y1 = ch * (scala.util.Random.nextInt.abs % 20 + 1) + y0

        val base = GridExtent[Int](Extent(x0, y0, x1, y1), CellSize(cw, ch))

        val xa = scala.util.Random.nextDouble * (x1 - x0) + x0
        val xb = scala.util.Random.nextDouble * (x1 - x0) + x0
        val ya = scala.util.Random.nextDouble * (y1 - y0) + y0
        val yb = scala.util.Random.nextDouble * (y1 - y0) + y0

        val ex = Extent(min(xa, xb), min(ya, yb), max(xa, xb), max(ya, yb))

        val aligned = base.createAlignedGridExtent(ex)

        val result = aligned.extent.contains(ex) && isWhole(aligned.extent.width / cw) && isWhole(aligned.extent.height / ch)

        if (!result) {
          println(s"Failed check: \n\tReference: $base\n\tOriginal extent: $ex\n\tAligned extent: ${aligned.extent}")
        }

        result
      }).reduce(_ && _) should be (true)
    }

    it("should allow aligned grid creation for grid with anchor point") {
      def isWhole(x: Double): Boolean = (x.round - x).abs < geotrellis.util.Constants.FLOAT_EPSILON

      (for (i <- (0 to 10000).toSeq) yield {
        val x0 = scala.util.Random.nextDouble * 100
        val y0 = scala.util.Random.nextDouble * 100
        val x1 = (scala.util.Random.nextInt.abs % 20 + 1) + x0
        val y1 = (scala.util.Random.nextInt.abs % 20 + 1) + y0

        val base = GridExtent[Int](Extent(x0, y0, x1, y1), CellSize(1.0, 1.0))

        val xa = scala.util.Random.nextDouble * (x1 - x0) + x0
        val xb = scala.util.Random.nextDouble * (x1 - x0) + x0
        val ya = scala.util.Random.nextDouble * (y1 - y0) + y0
        val yb = scala.util.Random.nextDouble * (y1 - y0) + y0

        val ex = Extent(min(xa, xb), min(ya, yb), max(xa, xb), max(ya, yb))

        val aligned = base.createAlignedGridExtent(ex, Point(0,0))

        val result = aligned.extent.contains(ex) && isWhole(aligned.extent.width) && isWhole(aligned.extent.height)

        if (!result) {
          println(s"Failed check: \n\tReference: $base\n\tOriginal extent: $ex\n\tAligned extent: ${aligned.extent}")
        }

        result
      }).reduce(_ && _) should be (true)
    }
  }
}
