import Common.{getCString, getCStringLength}
import Operation._
import jp.osscons.opensourcecobol.libcobj.data.CobolDataStorage
import ConstValues._

import java.nio.ByteBuffer
import java.sql.{ParameterMetaData, PreparedStatement, Types}
import scala.collection.immutable.Queue

class SQLVar (val sqlVarType: Int,
              val length: Int,
              val power: Int,
              val addr: Option[CobolDataStorage],
              val data: Option[CobolDataStorage],
              val realData: Option[CobolDataStorage],
              val realDataLength: Int){

  def setSqlVarType(sqlVarType: Int): SQLVar = new SQLVar(sqlVarType, length, power, addr, data, realData, realDataLength)
  def setLength(length: Int): SQLVar = new SQLVar(sqlVarType, length, power, addr, data, realData, realDataLength)
  def setPower(power: Int): SQLVar = new SQLVar(sqlVarType, length, power, addr, data, realData, realDataLength)
  def setAddr(addr: Option[CobolDataStorage]): SQLVar = new SQLVar(sqlVarType, length, power, addr, data, realData, realDataLength)
  def setData(data: Option[CobolDataStorage]): SQLVar = new SQLVar(sqlVarType, length, power, addr, data, realData, realDataLength)
  def setRealData(realData: Option[CobolDataStorage]): SQLVar = new SQLVar(sqlVarType, length, power, addr, data, realData, realDataLength)
  def setRealDataLength(realDataLength: Int): SQLVar = new SQLVar(sqlVarType, length, power, addr, data, realData, realDataLength)

  def getString: String = realData match {
    case None => ""
    case Some(storage) => new String(storage.getByteArray(0, this.realDataLength), "SHIFT-JIS")
  }

  def setParam(stmt: PreparedStatement, index: Int): Unit = {
    val str: String = this.getString
    val metaData: ParameterMetaData = stmt.getParameterMetaData()
    metaData.getParameterType(index) match {
      case Types.ARRAY => stmt.setString(index, str)
      case Types.BIGINT => stmt.setBigDecimal(index, new java.math.BigDecimal(str))
      case Types.BINARY => {}
      case Types.BIT => {}
      case Types.BLOB => stmt.setString(index, str)
      case Types.BOOLEAN => stmt.setBoolean(index, java.lang.Boolean.parseBoolean(str))
      case Types.CHAR => stmt.setString(index, str)
      case Types.CLOB => {}
      case Types.DATALINK => {}
      case Types.DATE => stmt.setString(index, str)
      case Types.DECIMAL => stmt.setBigDecimal(index, new java.math.BigDecimal(str))
      case Types.DISTINCT => {}
      case Types.DOUBLE => stmt.setDouble(index, java.lang.Double.parseDouble(str))
      case Types.FLOAT => stmt.setFloat(index, java.lang.Float.parseFloat(str))
      case Types.INTEGER => stmt.setInt(index, Integer.parseInt(str))
      case Types.JAVA_OBJECT => stmt.setObject(index, str)
      case Types.LONGNVARCHAR => stmt.setString(index, str)
      case Types.LONGVARBINARY => stmt.setString(index, str)
      case Types.LONGVARCHAR => stmt.setString(index, str)
      case Types.NCHAR => stmt.setString(index, str)
      case Types.NCLOB => {}
      case Types.NULL => {}
      case Types.NUMERIC => stmt.setDouble(index, java.lang.Double.parseDouble(str))
      case Types.NVARCHAR => stmt.setString(index, str)
      case Types.OTHER => {}
      case Types.REAL => stmt.setDouble(index, java.lang.Double.parseDouble(str))
      case Types.REF => {}
      case Types.REF_CURSOR => {}
      case Types.ROWID => {}
      case Types.SMALLINT => stmt.setInt(index, java.lang.Short.parseShort(str))
      case Types.SQLXML => {}
      case Types.STRUCT => {}
      case Types.TIME => stmt.setString(index, str)
      case Types.TIME_WITH_TIMEZONE => stmt.setString(index, str)
      case Types.TINYINT => stmt.setInt(index, Integer.parseInt(str))
      case Types.VARBINARY => {}
      case Types.VARCHAR => stmt.setString(index, str)
    }
  }
}

object SQLVar {
  def defaultValue = new SQLVar(0, 0, 0, None, None, None, 0)

  def initSqlVarQueue(): Operation[Unit] =
    resetSqlVarQueue()

  def resetSqlVarQueue(): Operation[Unit] = for {
    state <- getState
    _ <- setState( {
      val globalState = state.globalState
      val newGlobalState = globalState.setSqlVarQueue(Queue.empty).setSqlResVarQueue(Queue.empty)
      state.setGlobalState(newGlobalState)
    })
  } yield ()

  def showSqlVarQueue(varQueue : Queue[SQLVar]): Operation[Unit] = varQueue match {
    case x +: xs => for {
      _ <- logLn(s"${x.sqlVarType} ${x.length} ${x.power} ${x.addr}")
      _ <- showSqlVarQueue(xs)
    } yield ()
    case _ => operationPure(())
  }

  def addSqlVarQueue(sqlVarType: Int, length: Int, scale: Int, addr: Option[CobolDataStorage]): Operation[Unit] = {
    val sqlVar = new SQLVar(sqlVarType, length, scale, addr, None, None, length)
    for {
      state <- getState
      newSqlVar <- createRealData(sqlVar, 0)
      _ <- setState({
        val globalState = state.globalState
        val varList = globalState.sqlVarQueue
        val newGlobalState = globalState.setSqlVarQueue(varList ++ Queue(newSqlVar))
        state.setGlobalState(newGlobalState)
      })
    } yield ()
  }

  def addSqlResVarQueue(sqlVarType: Int, length: Int, scale: Int, addr: Option[CobolDataStorage]): Operation[Unit] = {
    val newSqlVar = new SQLVar(sqlVarType, length, scale, addr, None, None, length)
    for {
      state <- getState
      _ <- createRealData(newSqlVar, 0)
      _ <- setState({
        val globalState = state.globalState
        val resVarList = globalState.sqlResVarQueue
        val newGlobalState = globalState.setSqlResVarQueue(resVarList ++ Queue(newSqlVar))
        state.setGlobalState(newGlobalState)
      })
    } yield ()
  }

  //[remark] logを出すだけのメソッド?
  def createRealData(v: SQLVar, index: Int): Operation[SQLVar]= for {
    state <- getState
    retSQLVar <- {
      val occurs = state.globalState.occursInfo
      val newAddr = v.addr.map(_.getDataStorage(index * (
        if(occurs.isPresent) {
          occurs.length
        } else {
          v.length
        })))
      val x = v.setAddr(newAddr)
      x.sqlVarType match {
        //case OCDB_TYPE_UNSIGNED_NUMBER => createRealDataUnsignedNumber(x)
        case OCDB_TYPE_SIGNED_NUMBER_TC => createRealDataSignedNumberTc(x)
        //case OCDB_TYPE_SIGNED_NUMBER_LS => createRealDataSignedNumberLs(x)
        //case OCDB_TYPE_UNSIGNED_NUMBER_PD => createRealDataUnsignedNumberPd(x)
        //case OCDB_TYPE_SIGNED_NUMBER_PD => createRealDataSignedNumberPd(x)
        //case OCDB_TYPE_JAPANESE => createRealDataJapanese(x)
        //case OCDB_TYPE_ALPHANUMERIC_VARYING => createRealDataAlphanumericVarying(x)
        //case OCDB_TYPE_JAPANESE_VARYING => createRealDataJapaneseVarying(x)
        case _ => createRealDataDefault(x)
      }
    }
  } yield retSQLVar

  //[remark] TODO implemet methds below
  private def createRealDataUnsignedNumber(v: SQLVar): Operation[SQLVar] = {
    val data = new CobolDataStorage(v.length + TERMINAL_LENGTH)
    if(!v.addr.isEmpty) {
      data.memcpy(v.addr.getOrElse(nullDataStorage), v.length)
    }
    val realDataLength = if(v.power < 0) { v.length + 1 } else { v.length }
    val realData = new CobolDataStorage(realDataLength + 1)
    if(!v.data.isEmpty) {
      realData.memcpy(v.data.getOrElse(nullDataStorage), realDataLength)
    }

    if(v.power < 0) {
      insertDecimalPoint(realData, realDataLength, v.power)
    }

    val w = new SQLVar(v.sqlVarType, v.length, v.power, v.addr, Some(data), Some(realData), v.realDataLength)

    for {
      _ <- logLn(s"${w.sqlVarType} ${v.length}->${w.length}#data:${storageToString(w.data)}#realdata:${storageToString(w.realData)}")
    } yield w
  }

  //TODO improve the algorithm
  private def removeInitZeroes(data: CobolDataStorage, len: Int): Array[Byte] = {
    var i = 0
    if(data.getByte(i) == '-'.toByte) {
      i += 1
    }

    while(i < len && data.getByte(i) == '0') {
      i += 1
    }

    val digits = if(i == len) {
      val arr = new Array[Byte](1)
      arr(0) = '0'.toByte
      arr
    } else if(data.getByte(i) == '.'.toByte) {
      val arr = new Array[Byte](len - i + 1)
      arr(0) = '0'
      for(j <- i until len) {
        arr(j - i + 1) = data.getByte(j)
      }
      arr
     } else {
      val arr = new Array[Byte](len - i)
      for(j <- i until len) {
        arr(j - i) = data.getByte(j)
      }
      arr
     }

    if(data.getByte(0) == '-'.toByte) {
      val arr = new Array[Byte](digits.length + 1)
      arr(0) = '-'
      for(i <- 1 until arr.length) {
        arr(i) = digits(i - 1)
      }
      arr
    } else {
      digits
    }
  }

  // TODO improve the algorighm
  private def createRealDataSignedNumberTc(v: SQLVar):  Operation[SQLVar] =  {
    val data = new CobolDataStorage(v.length + TERMINAL_LENGTH)
    data.memcpy(v.addr.getOrElse(nullDataStorage), v.length)

    val realDataLength = if(v.power < 0) {
      SIGN_LENGTH + v.length + 1
    } else {
      SIGN_LENGTH + v.length
    }
    val digitFirstIndex = realDataLength - SIGN_LENGTH - v.length

    val realData = new CobolDataStorage(realDataLength)
    realData.getSubDataStorage(SIGN_LENGTH).memcpy(data, v.length)

    val signByte = realData.getByte(v.length + SIGN_LENGTH - 1)
    if(0x70.toByte <= signByte && signByte <= 0x79) {
      realData.setByte(0, '-'.toByte)
      realData.setByte(v.length + SIGN_LENGTH - 1, (signByte - 0x40.toByte).toByte)
    } else {
      realData.setByte(0, '0'.toByte)
    }

    if(v.power < 0) {
      val pointIndex = realDataLength + v.power - 1
      if(digitFirstIndex < pointIndex) {
        for(i <- (realDataLength - 1) to (pointIndex + 1) by -1) {
          realData.setByte(i, realData.getByte(i - 1))
        }
        realData.setByte(pointIndex, '.'.toByte)
      }
    }

    val bytes = removeInitZeroes(realData, realDataLength)
    val storage = new CobolDataStorage(bytes)
    operationPure(v.setRealData(Some(storage)).setRealDataLength(bytes.length))
  }

  private def createRealDataSignedNumberLs(v: SQLVar): Operation[SQLVar] = {
    val data = new CobolDataStorage(v.length + SIGN_LENGTH + TERMINAL_LENGTH)
    if(!v.addr.isEmpty) {
      data.memcpy(v.addr.getOrElse(nullDataStorage), v.length + SIGN_LENGTH)
    }
    val realDataLength = SIGN_LENGTH + (if(v.power < 0) { v.length + 1 } else { v.length })
    val realData = new CobolDataStorage(realDataLength)
    if(!v.data.isEmpty) {
      realData.memcpy(v.data.getOrElse(nullDataStorage), realDataLength)
    }

    if(v.power < 0) {
      insertDecimalPoint(realData, realDataLength, v.power)
    }

    val w = new SQLVar(v.sqlVarType, v.length, v.power, v.addr, Some(data), Some(realData), v.length)

    for {
      _ <- logLn(s"${w.sqlVarType} ${v.length}->${w.length}#data:${storageToString(w.data)}#realdata:${storageToString(w.realData)}")
    } yield w
  }

  private def createRealDataUnsignedNumberPd(v: SQLVar): Operation[SQLVar] = operationPure(v)
  private def createRealDataSignedNumberPd(v: SQLVar): Operation[SQLVar] = operationPure(v)

  private def createRealDataJapanese(v: SQLVar): Operation[SQLVar] = operationPure(v)
  private def createRealDataAlphanumericVarying(v: SQLVar): Operation[SQLVar] = operationPure(v)
  private def createRealDataJapaneseVarying(v: SQLVar): Operation[SQLVar] = operationPure(v)
  private def createRealDataDefault(v: SQLVar): Operation[SQLVar] = {
    val data = new CobolDataStorage(v.length)
    val realData = new CobolDataStorage(v.length)
    data.memcpy(v.addr.getOrElse(nullDataStorage), v.length)
    realData.memcpy(v.addr.getOrElse(nullDataStorage), v.length)
    operationPure(v.setData(Some(data)).setRealData(Some(realData)))
  }

  private def storageToString(storage: Option[CobolDataStorage]): String = {
    storage.map(s => getCString(s).getOrElse("")).getOrElse("")
  }

  /*[remark] 実行時エラーが発生しやすいため要デバッグ */
  private def insertDecimalPoint(data: CobolDataStorage, dataSize: Int, power: Int): Unit = {
    val beforeLength = getCStringLength(data)
    val afterLength = beforeLength + 1
    val nDecimalPlacePlaces = -power
    if(dataSize < afterLength || nDecimalPlacePlaces <= 0 || nDecimalPlacePlaces >= beforeLength) {
      return
    }
    for(i <- 0 to nDecimalPlacePlaces - 1) {
      data.setByte(afterLength - i,
        data.getByte(beforeLength - i))
    }
    data.setByte(beforeLength - nDecimalPlacePlaces, '.'.toByte)
  }

  private def typeTcIsPositive(storage: CobolDataStorage): Operation[Boolean] = {
    val lastChar = storage.getByte(0)
    if(lastChar >= '0'.toByte && lastChar <= '9'.toByte) {
      return operationPure(true)
    }
    for(i <- 0 to TYPE_TC_NEGATIVE_FINAL_NUMBER_LEN - 1) {
      if(lastChar == TYPE_TC_NEGATIVE_FINAL_NUMBER.charAt(i)) {
        storage.setByte(0, s"${i}".charAt(0).toByte)
        return operationPure(false)
      }
    }

    storage.setByte(0, 0)
    for {
      _ <- logLn(s"no final_number found: ${lastChar.toChar}")
    } yield true
  }
}