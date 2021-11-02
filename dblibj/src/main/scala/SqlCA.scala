import SqlCA.SQLERRMC_LEN
import jp.osscons.opensourcecobol.libcobj.data.CobolDataStorage

import java.nio.ByteBuffer
import scala.reflect.ClassManifest

class SqlCA (
              val caid: Array[Byte], //LEN = 8
              val abc: Int,
              val code: Int,
              val errml: Short,
              val errmc: Array[Byte], // LEN = SQLERRMC_LEN
              val errp: Array[Byte], //LEN = 8
              val errd: Array[Int], //LEN = 6
              val warn: Array[Byte], //LEN = 8
              val state: Array[Byte]) {
  def set(storage: CobolDataStorage): Unit = {
    var i = 0
    storage.set(caid)
    i += 8
    storage.getSubDataStorage(i).set(abc)
    i += 4
    storage.getSubDataStorage(i).set(code)
    i += 4
    storage.getSubDataStorage(i).set(errml)
  }

  def setCaid(caid: Array[Byte]): SqlCA = new SqlCA(caid, abc, code, errml, errmc, errp, errd, warn, state)
  def setAbc(abc: Int): SqlCA = new SqlCA(caid, abc, code, errml, errmc, errp, errd, warn, state)
  def setCode(code: Int): SqlCA = new SqlCA(caid, abc, code, errml, errmc, errp, errd, warn, state)
  def setErrml(errml: Short): SqlCA = new SqlCA(caid, abc, code, errml, errmc, errp, errd, warn, state)
  def setErrmc(errmc: Array[Byte]): SqlCA = {
    val arr: Array[Byte] = new Array[Byte](SQLERRMC_LEN)
    for(i <- 0 until Math.min(arr.length, errmc.length))
      arr(i) = errmc(i)
    new SqlCA(caid, abc, code, errml, arr, errp, errd, warn, state)
  }
  def setErrp(errp: Array[Byte]): SqlCA = new SqlCA(caid, abc, code, errml, errmc, errp, errd, warn, state)
  def setErrd(errd: Array[Int]): SqlCA = new SqlCA(caid, abc, code, errml, errmc, errp, errd, warn, state)
  def setWarn(warn: Array[Byte]): SqlCA = new SqlCA(caid, abc, code, errml, errmc, errp, errd, warn, state)
  def setState(state: Array[Byte]): SqlCA = new SqlCA(caid, abc, code, errml, errmc, errp, errd, warn, state)
}

object SqlCA {
  val SQLERRMC_LEN: Int = 70
  def defaultValue =
    new SqlCA(
      getArray(0, 8),
      0,
      0,
      0,
      getArray(0, 70),
      getArray(0, 8),
      getArray(0, 6),
      getArray(0, 8),
      getArray(0, 5))

  private def getArray[A : ClassManifest](value: A, length: Int): Array[A] = {
    var arr: Array[A] = new Array[A](length)
    for ( i <- 0 to length - 1) { arr(i) = value }
    arr
  }

  def storageToSqlCA(storage: CobolDataStorage): SqlCA = {
    var i = 0
    val caid: Array[Byte] = storage.getByteArray(i, 8)
    i += 8
    val abc: Int = ByteBuffer.wrap(storage.getByteArray(i, 4)).getInt()
    i += 4
    val code: Int = ByteBuffer.wrap(storage.getByteArray(i, 4)).getInt()
    i += 4
    val errml: Short = ByteBuffer.wrap(storage.getByteArray(i, 2)).getShort()
    i += 2
    val errmc: Array[Byte] = storage.getByteArray(i, 70)
    i += 70
    val errp: Array[Byte] = storage.getByteArray(i, 8)
    i += 8
    val errd: Array[Int] = Array(
      ByteBuffer.wrap(storage.getByteArray(i,    4)).getInt(),
      ByteBuffer.wrap(storage.getByteArray(i+4,  4)).getInt(),
      ByteBuffer.wrap(storage.getByteArray(i+8,  4)).getInt(),
      ByteBuffer.wrap(storage.getByteArray(i+12, 4)).getInt(),
      ByteBuffer.wrap(storage.getByteArray(i+16, 4)).getInt(),
      ByteBuffer.wrap(storage.getByteArray(i+20, 4)).getInt(),
    )
    i += 4 * 6
    val warn: Array[Byte] = storage.getByteArray(i, 8)
    i += 8
    val state: Array[Byte] = storage.getByteArray(i, 5)
    new SqlCA(caid, abc, code, errml, errmc, errp, errd, warn, state)
  }
}
