import Common.{getCString, getCStringLength}
import Operation._
import jp.osscons.opensourcecobol.libcobj.data.CobolDataStorage
import ConstValues._

import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets
import java.nio.charset.Charset
import java.sql.{ParameterMetaData, PreparedStatement, Types}
import scala.collection.immutable.Queue

class SQLVar(
    val sqlVarType: Int,
    val length: Int,
    val power: Int,
    val addr: Option[CobolDataStorage],
    val data: Option[CobolDataStorage],
    val realData: Option[CobolDataStorage],
    val realDataLength: Int
) {

  def setSqlVarType(sqlVarType: Int): SQLVar =
    new SQLVar(sqlVarType, length, power, addr, data, realData, realDataLength)
  def setLength(length: Int): SQLVar =
    new SQLVar(sqlVarType, length, power, addr, data, realData, realDataLength)
  def setPower(power: Int): SQLVar =
    new SQLVar(sqlVarType, length, power, addr, data, realData, realDataLength)
  def setAddr(addr: Option[CobolDataStorage]): SQLVar =
    new SQLVar(sqlVarType, length, power, addr, data, realData, realDataLength)
  def setData(data: Option[CobolDataStorage]): SQLVar =
    new SQLVar(sqlVarType, length, power, addr, data, realData, realDataLength)
  def setRealData(realData: Option[CobolDataStorage]): SQLVar =
    new SQLVar(sqlVarType, length, power, addr, data, realData, realDataLength)
  def setRealDataLength(realDataLength: Int): SQLVar =
    new SQLVar(sqlVarType, length, power, addr, data, realData, realDataLength)

  def getString: String = realData match {
    case None => ""
    case Some(storage) => {
      val bytes = storage.getByteArray(0, this.realDataLength)
      new String(bytes, Charset.forName("SHIFT-JIS"))
    }
  }

  // scalastyle:off method.length
  // scalastyle:off cyclomatic.complexity
  def setParam(
      stmt: PreparedStatement,
      index: Int,
      metaData: ParameterMetaData
  ): Unit = {
    val str: String = this.getString
    metaData.getParameterType(index) match {
      case Types.ARRAY => stmt.setString(index, str)
      case Types.BIGINT =>
        stmt.setBigDecimal(index, new java.math.BigDecimal(str))
      case Types.BINARY => {}
      case Types.BIT => {
        stmt.setBoolean(index, java.lang.Boolean.valueOf(str))
      }
      case Types.BLOB => stmt.setString(index, str)
      case Types.BOOLEAN =>
        stmt.setBoolean(index, java.lang.Boolean.valueOf(str))
      case Types.CHAR     => stmt.setString(index, str)
      case Types.CLOB     => {}
      case Types.DATALINK => {}
      case Types.DATE     => stmt.setDate(index, java.sql.Date.valueOf(str))
      case Types.DECIMAL =>
        stmt.setBigDecimal(index, new java.math.BigDecimal(str))
      case Types.DISTINCT => {}
      case Types.DOUBLE =>
        stmt.setDouble(index, java.lang.Double.parseDouble(str))
      case Types.FLOAT => stmt.setFloat(index, java.lang.Float.parseFloat(str))
      case Types.INTEGER       => stmt.setInt(index, Integer.parseInt(str))
      case Types.JAVA_OBJECT   => stmt.setObject(index, str)
      case Types.LONGNVARCHAR  => stmt.setString(index, str)
      case Types.LONGVARBINARY => stmt.setString(index, str)
      case Types.LONGVARCHAR   => stmt.setString(index, str)
      case Types.NCHAR         => stmt.setString(index, str)
      case Types.NCLOB         => {}
      case Types.NULL          => {}
      case Types.NUMERIC =>
        stmt.setDouble(index, java.lang.Double.parseDouble(str))
      case Types.NVARCHAR => stmt.setString(index, str)
      case Types.OTHER    => stmt.setString(index, str)
      case Types.REAL =>
        stmt.setDouble(index, java.lang.Double.parseDouble(str))
      case Types.REF        => {}
      case Types.REF_CURSOR => {}
      case Types.ROWID      => {}
      case Types.SMALLINT => stmt.setInt(index, java.lang.Integer.parseInt(str))
      case Types.SQLXML   => {}
      case Types.STRUCT   => {}
      case Types.TIME     => stmt.setTime(index, java.sql.Time.valueOf(str))
      case Types.TIME_WITH_TIMEZONE =>
        stmt.setTime(index, java.sql.Time.valueOf(str))
      case Types.TIMESTAMP =>
        stmt.setTimestamp(index, java.sql.Timestamp.valueOf(str))
      case Types.TIMESTAMP_WITH_TIMEZONE =>
        stmt.setTimestamp(index, java.sql.Timestamp.valueOf(str))
      case Types.TINYINT   => stmt.setInt(index, Integer.parseInt(str))
      case Types.VARBINARY => {}
      case Types.VARCHAR   => stmt.setString(index, str)
    }
  }
  // scalastyle:on method.length
  // scalastyle:on cyclomatic.complexity
}

object SQLVar {
  def defaultValue: SQLVar = new SQLVar(0, 0, 0, None, None, None, 0)

  def resetSqlVarQueue(state: OCDBState): Unit = {
    val globalState = state.globalState
    val newGlobalState =
      globalState.setSqlVarQueue(Queue.empty).setSqlResVarQueue(Queue.empty)
    state.updateGlobalState(newGlobalState)
  }

  def showSqlVarQueue(varQueue: Queue[SQLVar]): Unit = {
    for (x <- varQueue) {
      logLn(s"${x.sqlVarType} ${x.length} ${x.power} ${x.addr}")
    }
  }

  def addSqlVarQueue(
      sqlVarType: Int,
      length: Int,
      scale: Int,
      addr: Option[CobolDataStorage],
      state: OCDBState
  ): Unit = {
    val sqlVar = new SQLVar(sqlVarType, length, scale, addr, None, None, length)
    val newSqlVar = createRealData(sqlVar, 0, state)
    val globalState = state.globalState
    val varList = globalState.sqlVarQueue
    val newGlobalState = globalState.setSqlVarQueue(varList ++ Queue(newSqlVar))
    state.updateGlobalState(newGlobalState)
  }

  def addSqlResVarQueue(
      sqlVarType: Int,
      length: Int,
      scale: Int,
      addr: Option[CobolDataStorage],
      state: OCDBState
  ): Unit = {
    val newSqlVar =
      new SQLVar(sqlVarType, length, scale, addr, None, None, length)
    createRealData(newSqlVar, 0, state)
    val globalState = state.globalState
    val resVarList = globalState.sqlResVarQueue
    val newGlobalState =
      globalState.setSqlResVarQueue(resVarList ++ Queue(newSqlVar))
    state.updateGlobalState(newGlobalState)
  }

  // [remark] logを出すだけのメソッド?
  def createRealData(v: SQLVar, index: Int, state: OCDBState): SQLVar = {
    val occurs = state.globalState.occursInfo
    val newAddr = v.addr.map(_.getDataStorage(index * (if (occurs.isPresent) {
                                                         occurs.length
                                                       } else {
                                                         v.length
                                                       })))
    val x = v.setAddr(newAddr)
    x.sqlVarType match {
      case OCDB_TYPE_UNSIGNED_NUMBER    => createRealDataUnsignedNumber(x)
      case OCDB_TYPE_SIGNED_NUMBER_TC   => createRealDataSignedNumberTc(x)
      case OCDB_TYPE_SIGNED_NUMBER_LS   => createRealDataSignedNumberLs(x)
      case OCDB_TYPE_UNSIGNED_NUMBER_PD => createRealDataUnsignedNumberPd(x)
      case OCDB_TYPE_SIGNED_NUMBER_PD   => createRealDataSignedNumberPd(x)
      case OCDB_TYPE_JAPANESE           => createRealDataJapanese(x)
      // case OCDB_TYPE_ALPHANUMERIC_VARYING => createRealDataAlphanumericVarying(x)
      // case OCDB_TYPE_JAPANESE_VARYING => createRealDataJapaneseVarying(x)
      case _ => createRealDataDefault(x)
    }
  }

  private def createRealDataUnsignedNumber(v: SQLVar): SQLVar = {
    val data = new CobolDataStorage(v.length + TERMINAL_LENGTH)
    data.memcpy(v.addr.getOrElse(nullDataStorage), v.length)

    val realDataLength = if (v.power < 0) {
      v.length + 1
    } else {
      v.length + v.power
    }
    val digitFirstIndex = realDataLength - v.length

    val realData = new CobolDataStorage(realDataLength)
    realData.memset('0'.toByte, realDataLength)
    realData.memcpy(data, v.length)

    if (v.power < 0) {
      val pointIndex = realDataLength + v.power - 1
      if (digitFirstIndex < pointIndex) {
        for (i <- (realDataLength - 1) to (pointIndex + 1) by -1) {
          realData.setByte(i, realData.getByte(i - 1))
        }
        realData.setByte(pointIndex, '.'.toByte)
      }
    }

    val bytes = removeInitZeroes(realData, realDataLength)
    val storage = new CobolDataStorage(bytes)
    v.setRealData(Some(storage)).setRealDataLength(bytes.length)
  }

  private def firstIndexOfNonZeroByte(data: CobolDataStorage, len: Int): Int = {
    var i = 0
    if (data.getByte(i) == '-'.toByte || data.getByte(i) == '+'.toByte) {
      i += 1
    }

    while (i < len && data.getByte(i) == '0') {
      i += 1
    }
    i
  }

  // TODO improve the algorithm
  private def removeInitZeroes(
      data: CobolDataStorage,
      len: Int
  ): Array[Byte] = {
    val i = firstIndexOfNonZeroByte(data, len)

    val digits = if (i == len) {
      val arr = new Array[Byte](1)
      arr(0) = '0'.toByte
      arr
    } else if (data.getByte(i) == '.'.toByte) {
      val arr = new Array[Byte](len - i + 1)
      arr(0) = '0'
      for (j <- i until len) {
        arr(j - i + 1) = data.getByte(j)
      }
      arr
    } else {
      val arr = new Array[Byte](len - i)
      for (j <- i until len) {
        arr(j - i) = data.getByte(j)
      }
      arr
    }

    if (data.getByte(0) == '-'.toByte) {
      val arr = new Array[Byte](digits.length + 1)
      arr(0) = '-'
      for (i <- 1 until arr.length) {
        arr(i) = digits(i - 1)
      }
      arr
    } else {
      digits
    }
  }

  // TODO improve the algorighm
  private def createRealDataSignedNumberTc(v: SQLVar): SQLVar = {
    val data = new CobolDataStorage(v.length + TERMINAL_LENGTH)
    data.memcpy(v.addr.getOrElse(nullDataStorage), v.length)

    val realDataLength = if (v.power < 0) {
      SIGN_LENGTH + v.length + 1
    } else {
      SIGN_LENGTH + v.length + v.power
    }
    val digitFirstIndex = realDataLength - SIGN_LENGTH - v.length

    val realData = new CobolDataStorage(realDataLength)
    realData.memset('0'.toByte, realDataLength)
    realData.getSubDataStorage(SIGN_LENGTH).memcpy(data, v.length)

    val signByte = realData.getByte(v.length + SIGN_LENGTH - 1)
    if (0x70.toByte <= signByte && signByte <= 0x79) {
      realData.setByte(0, '-'.toByte)
      realData.setByte(
        v.length + SIGN_LENGTH - 1,
        (signByte - 0x40.toByte).toByte
      )
    }

    if (v.power < 0) {
      val pointIndex = realDataLength + v.power - 1
      if (digitFirstIndex < pointIndex) {
        for (i <- (realDataLength - 1) to (pointIndex + 1) by -1) {
          realData.setByte(i, realData.getByte(i - 1))
        }
        realData.setByte(pointIndex, '.'.toByte)
      }
    }

    val bytes = removeInitZeroes(realData, realDataLength)
    val storage = new CobolDataStorage(bytes)
    v.setRealData(Some(storage)).setRealDataLength(bytes.length)
  }

  // TODO fix
  private def createRealDataSignedNumberLs(v: SQLVar): SQLVar = {
    val data = v.addr.getOrElse(nullDataStorage)
    val rawStr = new String(
      v.addr.getOrElse(nullDataStorage).getByteArray(0, v.length + 1)
    )
    val convertedStr = if (v.power < 0) {
      val (fst, snd) = rawStr.splitAt(v.length + v.power)
      fst + "." + snd
    } else {
      rawStr + "0" * v.power
    }

    val realData = new CobolDataStorage(convertedStr.getBytes())
    v.setRealData(Some(realData)).setRealDataLength(convertedStr.length)
  }

  private def createRealDataUnsignedNumberPd(v: SQLVar): SQLVar = {
    val data = v.addr.getOrElse(nullDataStorage)
    val len = (v.length / 2).toInt + 1

    val bytes = new Array[Byte](v.length)
    for (i <- 0 to (len - 1)) {
      val b = data.getByte(i).toInt
      val a0 = (((b & 0xf0) >> 4) + '0').toByte
      val a1 = ((b & 0x0f) + '0').toByte
      if (v.length % 2 == 0) {
        if (i == 0) {
          bytes(0) = a1
        } else if (i == len - 1) {
          bytes(v.length - 1) = a0
        } else {
          bytes(2 * i - 1) = a0
          bytes(2 * i) = a1
        }
      } else {
        if (i == len - 1) {
          bytes(v.length - 1) = a0
        } else {
          bytes(2 * i) = a0
          bytes(2 * i + 1) = a1
        }
      }
    }

    // 0.00XYZW の場合
    var (realData, realDataLen) = if (-v.power > v.length) {
      val realDataLen = v.power + 2
      var realData = new CobolDataStorage(realDataLen)
      realData.memset('0'.toByte, realDataLen)
      realData.setByte(1, '.'.toByte)
      realData
        .getSubDataStorage(realDataLen - bytes.length)
        .memcpy(bytes, bytes.length)
      (realData, realDataLen)
      // XY.ZWの場合
    } else if (-v.power > 0) {
      val tmpDataLen = v.length + 1
      var tmpData = new CobolDataStorage(tmpDataLen)
      tmpData.memcpy(bytes, tmpDataLen + v.power - 1)
      tmpData.setByte(tmpDataLen + v.power - 1, '.'.toByte)
      for (i <- 0 to -v.power - 1) {
        tmpData.setByte(
          tmpDataLen + v.power + i,
          bytes(bytes.length + v.power + i)
        )
      }
      val realBytes = removeInitZeroes(tmpData, tmpDataLen)
      (new CobolDataStorage(realBytes), realBytes.length)
      // XYZWやXYZW000の場合
    } else {
      val tmpDataLen = v.length + v.power
      var tmpData = new CobolDataStorage(tmpDataLen)
      tmpData.memset('0'.toByte, tmpDataLen)
      tmpData.memcpy(bytes, v.length)
      val realBytes = removeInitZeroes(tmpData, tmpDataLen)
      (new CobolDataStorage(realBytes), realBytes.length)
    }

    v.setRealData(Some(realData)).setRealDataLength(realDataLen)
  }

  private def createRealDataSignedNumberPd(v: SQLVar): SQLVar = {
    val data = v.addr.getOrElse(nullDataStorage)
    val len = (v.length / 2).toInt + 1
    val sign = if ((data.getByte(len - 1) & 0x0f) == 0x0d) { -1 }
    else { 1 }

    val bytes = new Array[Byte](v.length)
    for (i <- 0 to (len - 1)) {
      val b = data.getByte(i).toInt
      val a0 = (((b & 0xf0) >> 4) + '0').toByte
      val a1 = ((b & 0x0f) + '0').toByte
      if (v.length % 2 == 0) {
        if (i == 0) {
          bytes(0) = a1
        } else if (i == len - 1) {
          bytes(v.length - 1) = a0
        } else {
          bytes(2 * i - 1) = a0
          bytes(2 * i) = a1
        }
      } else {
        if (i == len - 1) {
          bytes(v.length - 1) = a0
        } else {
          bytes(2 * i) = a0
          bytes(2 * i + 1) = a1
        }
      }
    }

    var (realData, realDataLen) = setDataRealDataSignedNumberPd(v, bytes, sign)

    v.setRealData(Some(realData)).setRealDataLength(realDataLen)
  }

  private def setDataRealDataSignedNumberPd(
      v: SQLVar,
      bytes: Array[Byte],
      sign: Int
  ): (CobolDataStorage, Int) = {
    // 0.00XYZW の場合
    var (realData, realDataLen) = if (-v.power > v.length) {
      val realDataLen = v.power + 2
      var realData = new CobolDataStorage(realDataLen)
      realData.memset('0'.toByte, realDataLen)
      realData.setByte(1, '.'.toByte)
      realData
        .getSubDataStorage(realDataLen - bytes.length)
        .memcpy(bytes, bytes.length)
      (realData, realDataLen)
      // XY.ZWの場合
    } else if (-v.power > 0) {
      val tmpDataLen = v.length + 1
      var tmpData = new CobolDataStorage(tmpDataLen)
      tmpData.memcpy(bytes, tmpDataLen + v.power - 1)
      tmpData.setByte(tmpDataLen + v.power - 1, '.'.toByte)
      for (i <- 0 to -v.power - 1) {
        tmpData.setByte(
          tmpDataLen + v.power + i,
          bytes(bytes.length + v.power + i)
        )
      }
      val realBytes = removeInitZeroes(tmpData, tmpDataLen)
      (new CobolDataStorage(realBytes), realBytes.length)
      // XYZWやXYZW000の場合
    } else {
      val tmpDataLen = v.length + v.power
      var tmpData = new CobolDataStorage(tmpDataLen)
      tmpData.memset('0'.toByte, tmpDataLen)
      tmpData.memcpy(bytes, v.length)
      val realBytes = removeInitZeroes(tmpData, tmpDataLen)
      (new CobolDataStorage(realBytes), realBytes.length)
    }
    if (sign < 0) {
      var tmpData = new CobolDataStorage(realDataLen + 1)
      tmpData.getSubDataStorage(1).memcpy(realData, realDataLen)
      tmpData.setByte(0, '-'.toByte)
      realData = tmpData
      realDataLen += 1
    }
    (realData, realDataLen)
  }

  private def createRealDataJapanese(v: SQLVar): SQLVar = {
    val realData = new CobolDataStorage(v.length * 2)
    realData.memcpy(v.addr.getOrElse(nullDataStorage), v.length * 2)
    v.setRealData(Some(realData)).setRealDataLength(v.length * 2)
  }

  private def createRealDataAlphanumericVarying(v: SQLVar): SQLVar = v
  private def createRealDataJapaneseVarying(v: SQLVar): SQLVar = v

  private def createRealDataDefault(v: SQLVar): SQLVar = {
    val data = new CobolDataStorage(v.length)
    val realData = new CobolDataStorage(v.length)
    data.memcpy(v.addr.getOrElse(nullDataStorage), v.length)
    realData.memcpy(v.addr.getOrElse(nullDataStorage), v.length)
    v.setRealData(Some(realData)).setRealDataLength(v.length)
  }

  private def storageToString(storage: Option[CobolDataStorage]): String = {
    storage.map(s => getCString(s).getOrElse("")).getOrElse("")
  }

  /*[remark] 実行時エラーが発生しやすいため要デバッグ */
  private def insertDecimalPoint(
      data: CobolDataStorage,
      dataSize: Int,
      power: Int
  ): Unit = {
    val beforeLength = getCStringLength(data)
    val afterLength = beforeLength + 1
    val nDecimalPlacePlaces = -power
    if (
      dataSize < afterLength || nDecimalPlacePlaces <= 0 || nDecimalPlacePlaces >= beforeLength
    ) {
      return
    }
    for (i <- 0 to nDecimalPlacePlaces - 1) {
      data.setByte(afterLength - i, data.getByte(beforeLength - i))
    }
    data.setByte(beforeLength - nDecimalPlacePlaces, '.'.toByte)
  }
}
