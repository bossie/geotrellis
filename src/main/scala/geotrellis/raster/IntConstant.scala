package geotrellis.raster

import geotrellis._

import spire.syntax.cfor._

final case class IntConstant(n: Int, cols: Int, rows: Int) extends RasterData {
  def getType = TypeInt
  def apply(i: Int) = n
  def applyDouble(i: Int) = n.toDouble
  def length = cols * rows
  def alloc(cols: Int, rows: Int) = IntArrayRasterData.empty(cols, rows)
  def mutable = IntArrayRasterData(Array.ofDim[Int](length).fill(n), cols, rows)
  def copy = this

  override def combine(other: RasterData)(f: (Int, Int) => Int) = other.map(z => f(n, z))
  override def map(f: Int => Int) = IntConstant(f(n), cols, rows)

  override def foreach(f: Int => Unit) {
    var i = 0
    val len = length
    while (i < len) { f(n); i += 1 }
  }

  override def combineDouble(other: RasterData)(f: (Double, Double) => Double) = other.mapDouble(z => f(n, z))
  override def mapDouble(f: Double => Double) = DoubleConstant(f(n), cols, rows)
  override def foreachDouble(f: Double => Unit) = foreach(z => f(z))

  def force(): RasterData = {
    val forcedData = RasterData.allocByType(getType, cols, rows)
    cfor(0)(_ < cols, _ + 1) { col =>
      cfor(0)(_ < rows, _ + 1) { row =>
        forcedData.set(col, row, n)
      }
    }
    forcedData
  }

  def toArrayByte: Array[Byte] = Array(n.toByte)

  def warp(current:RasterExtent,target:RasterExtent):RasterData =
    IntConstant(n,target.cols,target.rows)
}