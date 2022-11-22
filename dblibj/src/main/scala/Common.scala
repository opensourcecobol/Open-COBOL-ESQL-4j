import ConstValues._
import Operation._
import Cursor._

import java.sql._
import ConnectionInfo.ConnectionMap
import jp.osscons.opensourcecobol.libcobj.data.CobolDataStorage

import java.nio.ByteBuffer
import scala.collection.immutable.Queue
import scala.runtime.Statics

object Common {
  var internalState: GlobalState = GlobalState.initialGlobalState

  def initiSqlCA(): Operation[Unit] =
    updateState(state => state.setSqlCA(SqlCA.defaultValue))

  def ocesqlExec(id: Int, query: Option[String]): Operation[Unit] = {
    val procEmpty = setLibErrorStatus(OCDB_EMPTY()).map(_ => ())
    for {
      _ <- initSqlca()
      _ <- query match {
        case None => procEmpty
        case Some(q) if q.isEmpty => procEmpty
        case Some(q) => for {
          _ <- OCDBExec(id, q)
          success <- setResultStatus(id)
          _ <- when(success && (q == "COMMIT" || q == "ROLLBACK"), for {
            _ <- clearCursorMap(id)
            _ <- OCDBExec(id, "BEGIN")
            _ <- setResultStatus(id)
            } yield ())
        } yield ()
      }
    } yield ()
  }

  def ocesqlExecParams(id: Int, query: Option[String], nParams: Int): Operation[Unit] = (for {
    state <- operationCPure(getState)
    _ <- operationCPure(initSqlca())

    _ <- whenExecuteAndExit(query.getOrElse("").length == 0 || nParams == 0, for {
        _ <- setLibErrorStatus(OCDB_EMPTY())
      } yield ())

    _ <- whenExecuteAndExit(state.globalState.sqlVarQueue.size > nParams, for {
      _ <- setLibErrorStatus(OCDB_TOO_MANY_ARGUMENTS())
      } yield ())

    _ <- whenExecuteAndExit(state.globalState.sqlVarQueue.size < nParams, for {
      _ <- setLibErrorStatus(OCDB_TOO_FEW_ARGUMENTS())
      } yield ())

     _ <- operationCPure(OCDBExecParams(id, query.getOrElse(""), state.globalState.sqlVarQueue))
    result <- operationCPure(setResultStatus(id))

    _ <- whenExecute(result && (query.getOrElse("") == "COMMIT" || query.getOrElse("") == "ROLLBACK"), for {
      _ <- clearCursorMap(id)
      _ <- OCDBExec(id, "BEGIN")
      _ <- setResultStatus(id)
      } yield ())
  } yield ()) .eval

  //[remark] 一部未実装
  def ocesqlExecParamsOccurs(id: Int, query: Option[String], nParams: Int): Operation[Unit] =
    (for {
      state <- operationCPure(getState)
      _ <- operationCPure(initSqlca())

      //[remark] 条件を正しく書く
      _ <- whenExecuteAndExit(query.getOrElse("").length == 0 || nParams == 0 /*||*/ , for {
        _ <- setLibErrorStatus(OCDB_EMPTY())
      } yield ())
    } yield ()).eval

  def OCDBExec(id: Int, query: String): Operation[Unit] =
    (for {
      keyAndValue <- operationCPure(lookUpConnList(id))
      _ <- whenExecuteAndExit(keyAndValue.isEmpty, operationPure(()))

      kv <- operationCPure(operationPure(keyAndValue.getOrElse(("", ConnectionInfo.defaultValue))))
      key <- operationCPure(operationPure(kv._1))
      pConn <- operationCPure(operationPure(kv._2))

      //_ <- whenExecute(pConn.resAddr != OCDB_RES_DEFAULT_ADDRESS,
        //OCDB_PGClear(pConn.resAddr))
      result <- operationCPure(OCDB_PGExec(pConn.connAddr, query))
      _ <- operationCPure(setConnList(key, pConn.setResult(result)))
    } yield ()).eval

  def OCDBExecParams(id: Int, query: String, sqlVarQueue: Queue[SQLVar]): Operation[Unit] =
    lookUpConnList(id).flatMap(_ match {
      case None => logLn(s"id=${id},pConn=")
      case Some((key, pConn)) => for {
        _ <- logLn(s"id=${id},pConn=${pConn}")
        result <- OCDB_PGExecParam(pConn.connAddr, query, sqlVarQueue)
        _ <- updateState(state => {
          val globalState = state.globalState
          val newPConn = pConn.setResult(Right(ESuccess())).setResult(result)
          val newConnMap = globalState.connectionMap + (key -> newPConn)
          val newGlobalState = globalState.setConnectionMap(newConnMap)
          state.setGlobalState(newGlobalState)
        })
        _ <- when(result.isLeft, errorLogLn("PostgreSQL Result is null"))
      } yield ()
    })

  /**
   * Equivalent to OCDB_PGExec in dblib/ocpgsql.c
   * [remark] 決して実行されないと思われる処理を書いていない
   * @param connAddr
   * @param query
   * @return
   */
  def OCDB_PGExec(connAddr: Option[Connection], query: String): Operation[ExecResult] = for {
    _ <- logLn(s"CONNADDR ${connAddr}, EXEC SQL ${query}")
    res <- connAddr match {
      case None => operationPure(Left(new SQLException()))
      case Some(c) => exec(c, query)
    }
  } yield res

  def OCDB_Finish(id: Int): Operation[Unit] =
    lookUpConnList(id).flatMap(_ match {
      case None => operationPure(())
      case Some((key, pConn)) => for {
        _ <- OCDB_PGFinish(pConn.connAddr)
        _ <- logLn(s"connection id ${id} released.")
        _ <- freeConnLists(id)
      } yield ()
    })

  def OCDB_PGFinish(conn: Option[Connection]): Operation[Unit] = conn match {
    case None => operationPure(())
    case Some(c) => close(c)
  }

  //[remark] rollBakckOneModeに関連した処理の実装
  def OCDB_PGExecParam(connAddr: Option[Connection], query: String, params: Queue[SQLVar]): Operation[ExecResult] = for {
    _ <- logLn(s"CONNADDR: ${connAddr}, EXEC SQL: ${query}")
    res <- connAddr match {
      case None => operationPure(Left(new SQLException()))
      case Some(c) => execParam(c, query, params)
    }
  } yield res

  /**
   * Equivalent to look_up_conn_lists in dblib/ocpgsql.c
   * @param id
   * @return
   */
  def lookUpConnList(id: Int): Operation[Option[(String, ConnectionInfo)]] = for {
    state <- getState
  } yield {
    val connMap = state.globalState.connectionMap
    val filteredList = connMap.filter(t => t._2.id == id).toList
    filteredList match {
      case Nil => None
      case x :: _ => Some((x._1, x._2))
    }
  }

  def updateConnList(id: Int, newConn: ConnectionInfo): Operation[Unit] = for {
    state <- getState
    _ <- {
      val connectionMap = state.globalState.connectionMap
      val newConnectionMap = connectionMap.transform((_, conn) =>
        if(conn.id == id) { newConn } else { conn })
      val newGlobalState = state.globalState.setConnectionMap(newConnectionMap)
      val newState = state.setGlobalState(newGlobalState)
      setState(newState)
    }
  } yield ()

  def updateConnList(key: String, f: ConnectionInfo => ConnectionInfo): Operation[Unit] = for {
    state <- getState
    _ <- {
      val connectionMap = state.globalState.connectionMap
      val connection = connectionMap.get(key)
      connection match {
        case None => operationPure()
        case Some(c) => {
          val newConnectionMap = connectionMap.updated(key, f(c))
          val newGlobalState = state.globalState.setConnectionMap(newConnectionMap)
          val newState = state.setGlobalState(newGlobalState)
          setState(newState)
        }
      }
    }
  } yield ()

  def freeConnLists(id: Int): Operation[Unit] = for {
    state <- getState
    _ <- setState({
      val connectionMap = state.globalState.connectionMap
      val newConnectionMap = connectionMap.filter(_._2.id != id)
      val newGlobalState = state.globalState.setConnectionMap(newConnectionMap)
      state.setGlobalState(newGlobalState)
    })
  } yield ()

  def setConnList(key: String, c: ConnectionInfo): Operation[Unit] =
    updateConnList(key, _ => c)

  def setLibErrorStatus(errorCode: SqlCode): Operation[Int] =
    OCDB_PGSetLibErrorStatus(errorCode)

  def setResultStatus(id: Int): Operation[Boolean] =
    lookUpConnList(id).flatMap(_ match {
      case None => operationPure(false)
      case Some((_, pConn)) => OCDB_PGSetResultStatus(pConn.result)
    })

  /**
   * Implementation of OCDB_PGSetResultStatus in dblib/ocpgsql.c (Open-COBOL-ESQL)
   * Regardless of the pretious SQL execution, sqlErrorCode is zero.
   * If the pretious SQL execution fails, sqlState equals to PSQLState.
   * (https://github.com/pgjdbc/pgjdbc/blob/8be516d47ece60b7aeba5a9474b5cac1d538a04a/pgjdbc/src/main/java/org/postgresql/util/PSQLState.java)
   * @param connAddr
   * @return true if and only if the previous SQL execution is successful.
   */
  def OCDB_PGSetResultStatus(result: ExecResult): Operation[Boolean] = {
    val (sqlCode, sqlState) = result match {
      case Right(_) => {
        (OCPG_NO_ERROR, "00000")
      }
      case Left(e) => {
        val state = Option(e.getSQLState).getOrElse("     ")
        (sqlStateToSqlCode(state), state)
      }
    }

    val errMsgInfo = result match {
      case Left(e) => Option(e.getMessage) match {
        case Some(msg) => {
          val bytes = msg.getBytes
          Some((bytes, bytes.length))
        }
        case _ => None
      }
      case _ => None
    }

    for {
      // Output Log
      _ <- errMsgInfo match {
        case Some((msg, _)) => for {
          _ <- logLn(s"MESSAGE:${msg}")
          _ <- errorLogLn(String.format(
            "%d:%5s:%-70s",
            sqlCode, sqlState, msg))
        } yield ()
        case _ => operationPure()
      }

      // Update SQLCA
      _ <- updateState(state => {
        val sqlCA = state.sqlCA
        val tmpSqlCA = sqlCA.setCode(sqlCode).setState(sqlState.getBytes)
        val newSqlCA = errMsgInfo match {
          case Some((msg, len)) =>
            tmpSqlCA
              .setErrmc(msg)
              .setErrml(len.toShort)
          case _ => tmpSqlCA
        }
        state.setSqlCA(newSqlCA)
      })
    } yield result.isRight
  }

  def resolveCONNID(cid: String, connectionMap: ConnectionMap): Operation[Option[ConnectionInfo]] =
    connectionMap.get(cid) match {
      case None => operationPure(None)
      case Some(info) => for {
        _ <- logLn(s"return connid=${cid}")
      } yield Some(info)
    }

  def resolveCONNID(cid: String): Operation[Option[ConnectionInfo]] =
    getState.flatMap(state => Common.resolveCONNID(cid, state.globalState.connectionMap))

  def OCDB_PGSetLibErrorStatus(errorCode: SqlCode): Operation[Int] = {
    val (code, sqlState) = errorCode match {
      case OCDB_NO_ERROR() => (OCPG_NO_ERROR, "00000")
      case OCDB_NOT_FOUND() => (OCPG_NOT_FOUND, "02000")
      case OCDB_OUT_OF_MEMORY() => (OCPG_OUT_OF_MEMORY, "YE001")
      case OCDB_UNSUPPORTED() => (OCPG_UNSUPPORTED, "YE002")
      case OCDB_TOO_MANY_ARGUMENTS() => (OCPG_TOO_MANY_ARGUMENTS, "07001")
      case OCDB_TOO_FEW_ARGUMENTS() => (OCPG_TOO_FEW_ARGUMENTS, "07002")
      case OCDB_TOO_MANY_MATCHES() => (OCPG_TOO_MANY_MATCHES, "21000")
      case OCDB_DATA_FORMAT_ERROR() => (OCPG_DATA_FORMAT_ERROR, "42804")
      case OCDB_EMPTY() => (OCPG_EMPTY, "YE002")
      case OCDB_MISSING_INDICATOR() => (OCPG_MISSING_INDICATOR, "22002")
      case OCDB_NO_CONN() => (OCPG_NO_CONN, "08003")
      case OCDB_NOT_CONN() => (OCPG_NOT_CONN, "YE002")
      case OCDB_INVALID_STMT() => (OCPG_INVALID_STMT, "26000")
      case OCDB_INFORMIX_DUPLICATE_KEY() => (OCPG_INFORMIX_DUPLICATE_KEY, "23505")
      case OCDB_UNKNOWN_DESCRIPTOR() => (OCPG_UNKNOWN_DESCRIPTOR, "33000")
      case OCDB_INVALID_DESCRIPTOR_INDEX() => (OCPG_INVALID_DESCRIPTOR_INDEX, "07009")
      case OCDB_UNKNOWN_DESCRIPTOR_ITEM() => (OCPG_UNKNOWN_DESCRIPTOR_ITEM, "YE002")
      case OCDB_VAR_NOT_NUMERIC() => (OCPG_VAR_NOT_NUMERIC, "07006")
      case OCDB_VAR_NOT_CHAR() => (OCPG_VAR_NOT_CHAR, "07006")
      case OCDB_INFORMIX_SUBSELECT_NOT_ONE() => (OCPG_INFORMIX_SUBSELECT_NOT_ONE, "21000")
      case OCDB_PGSQL() => (OCPG_PGSQL, "     ")
      case OCDB_TRANS() => (OCPG_TRANS, "08007")
      case OCDB_CONNECT() => (OCPG_CONNECT, "08001")
      case OCDB_DUPLICATE_KEY() => (OCPG_DUPLICATE_KEY, "23505")
      case OCDB_SUBSELECT_NOT_ONE() => (OCPG_SUBSELECT_NOT_ONE, "21000")
      case OCDB_WARNING_UNKNOWN_PORTAL() => (OCPG_WARNING_UNKNOWN_PORTAL, "34000")
      case OCDB_WARNING_IN_TRANSACTION() => (OCPG_WARNING_IN_TRANSACTION, "25001")
      case OCDB_WARNING_NO_TRANSACTION() => (OCPG_WARNING_NO_TRANSACTION, "25P01")
      case OCDB_WARNING_PORTAL_EXISTS() => (OCPG_WARNING_PORTAL_EXISTS, "42P03")
      case OCDB_LOCK_ERROR() => (OCPG_LOCK_ERROR, "57033")
      case OCDB_JDD_ERROR() => (OCPG_JDD_ERROR, "     ")
      case _ => (OCDB_UNKNOWN_ERROR, "     ")
    }
    for {
      _ <- updateState(state => {
        val sqlCA = state.sqlCA
        val newSqlCA = if (code == OCPG_PGSQL) {
          sqlCA.setCode(code)
        } else {
          sqlCA.setCode(code).setState(sqlState.getBytes())
        }
        state.setSqlCA(newSqlCA)
      })
    } yield if (code < 0) { RESULT_ERROR } else { RESULT_SUCCESS }
  }

  def getCString(storage: CobolDataStorage): Option[String] = {
    try {
      var i = 0
      while(storage.getByte(i) != 0) {
        i += 1
      }
      Some(new String(storage.getByteArray(0, i), "SHIFT-JIS"))
    } catch {
      case e: Throwable => None
    }
  }

  def getCStringLength(storage: CobolDataStorage): Int = {
    try {
      var i = 0
      while(storage.getByte(i) != 0) {
        i += 1
      }
      i
    } catch {
      case e: Throwable => 0
    }
  }

  def initSqlca(): Operation[Unit] = for {
    state <- getState
    _ <- setState(state.setSqlCA(SqlCA.defaultValue))
  } yield ()

  //inpure function
  def createCobolDataLowValue(sv: SQLVar, index: Int, occursInfo: OccursInfo): Unit = {
    val addr = if(occursInfo.isPresent) {
      sv.addr.getOrElse(nullDataStorage).getSubDataStorage(index * occursInfo.length)
    } else {
      sv.addr.getOrElse(nullDataStorage).getSubDataStorage(index * sv.length)
    }
    addr.memset(0, sv.length)
  }

  //inpure function
  def createCobolData(sv: SQLVar, index: Int, resultData: scala.Array[Byte], occursInfo: OccursInfo): Unit = {
    val realLength = {
      val len = if(occursInfo.isPresent) {occursInfo.length} else {sv.length}
      sv.sqlVarType match {
        case OCDB_TYPE_UNSIGNED_NUMBER_PD | OCDB_TYPE_SIGNED_NUMBER_PD =>
          (len / 2).toInt + 1
        case _ => len
      }
    }
    val addr = sv.addr.getOrElse(nullDataStorage).getSubDataStorage(index * realLength)
    sv.sqlVarType match {
      case OCDB_TYPE_UNSIGNED_NUMBER => createCobolDataUnsignedNumber(sv, addr, index, resultData)
      case OCDB_TYPE_SIGNED_NUMBER_TC => createCobolDataSignedNumberTc(sv, addr, index, resultData)
      case OCDB_TYPE_SIGNED_NUMBER_LS => createCobolDataSignedNumberLs(sv, addr, index, resultData)
      case OCDB_TYPE_UNSIGNED_NUMBER_PD => createCobolDataUnsignedNumberPd(sv, addr, index, resultData)
      case OCDB_TYPE_SIGNED_NUMBER_PD => createCobolDataSignedNumberPd(sv, addr, index, resultData)
      case OCDB_TYPE_ALPHANUMERIC => createCobolDataAlphanumeric(sv, addr, index, resultData)
      case OCDB_TYPE_JAPANESE => createCobolDataJapanese(sv, addr, index, resultData)
      case OCDB_TYPE_ALPHANUMERIC_VARYING => createCobolDataAlphanumericVarying(sv, addr, index, resultData)
      case OCDB_TYPE_JAPANESE_VARYING => createCobolDataJapaneseVarying(sv, addr, index, resultData)
    }
  }
  
  //[TODO] improve the algorithm
  private def createCobolDataUnsignedNumber(sv: SQLVar, addr: CobolDataStorage, index: Int, str: scala.Array[Byte]): Unit = {
    val finalBuf: scala.Array[Byte] = new scala.Array(sv.length)
    val isNegative = str(0) == '-'.toByte
    val valueFirstIndex = if(isNegative) {1} else {0}
    val indexOfDecimalPoint = {
      val index = str.indexOf('.')
      if(index < 0) { str.length } else { index }
    }

    for(i <- 0 until finalBuf.length) {
      finalBuf(i) = '0'.toByte
    }

    if(sv.power >= 0) {
      for(i <- valueFirstIndex until indexOfDecimalPoint) {
        finalBuf(i + finalBuf.length - (indexOfDecimalPoint + sv.power)) = str(i)
      }
    } else {
      var i = sv.length + sv.power - 1
      var j = indexOfDecimalPoint - 1
      while(i >= 0 && j >= valueFirstIndex) {
        finalBuf(i) = str(j)
        i -= 1
        j -= 1
      }
      i = sv.length + sv.power
      j = indexOfDecimalPoint + 1
      while(i < sv.length && j < str.length) {
        finalBuf(i) = str(j)
        i += 1
        j += 1
      }
    }

    for(i <- 0 until finalBuf.length) {
      addr.setByte(i, finalBuf(i))
    }
  }

  private def createCobolDataSignedNumberTc(sv: SQLVar, addr: CobolDataStorage, index: Int, str: scala.Array[Byte]): Unit = {
    val finalBuf: scala.Array[Byte] = new scala.Array(sv.length)
    val isNegative = str(0) == '-'.toByte
    val valueFirstIndex = if(isNegative) {1} else {0}
    val indexOfDecimalPoint = {
      val index = str.indexOf('.')
      if(index < 0) { str.length } else { index }
    }

    for(i <- 0 until finalBuf.length) {
      finalBuf(i) = '0'.toByte
    }

    if(sv.power >= 0) {
      for(i <- valueFirstIndex until indexOfDecimalPoint) {
        finalBuf(i + finalBuf.length - (indexOfDecimalPoint + sv.power)) = str(i)
      }
    } else {
      var i = sv.length + sv.power - 1
      var j = indexOfDecimalPoint - 1
      while(i >= 0 && j >= valueFirstIndex) {
        finalBuf(i) = str(j)
        i -= 1
        j -= 1
      }
      i = sv.length + sv.power
      j = indexOfDecimalPoint + 1
      while(i < sv.length && j < str.length) {
        finalBuf(i) = str(j)
        i += 1
        j += 1
      }
    }

    if(isNegative) {
      val finalByte = finalBuf(finalBuf.length - 1)
      finalBuf(finalBuf.length - 1) = (finalByte + 0x40).toByte
    }
    for(i <- 0 until finalBuf.length) {
      addr.setByte(i, finalBuf(i))
    }
  }

  private def createCobolDataSignedNumberLs(sv: SQLVar, addr: CobolDataStorage, i: Int, str: scala.Array[Byte]): Unit = {
    val finalBuf: scala.Array[Byte] = new scala.Array(sv.length)
    val isNegative = str(0) == '-'.toByte
    val valueFirstIndex = if(isNegative) {1} else {0}
    val indexOfDecimalPoint = {
      val index = str.indexOf('.')
      if(index < 0) { str.length } else { index }
    }

    for(i <- 0 until finalBuf.length) {
      finalBuf(i) = '0'.toByte
    }

    if(sv.power >= 0) {
      for(i <- valueFirstIndex until indexOfDecimalPoint) {
        finalBuf(i + finalBuf.length - (indexOfDecimalPoint + sv.power)) = str(i)
      }
    } else {
      var i = sv.length + sv.power
      var j = indexOfDecimalPoint - 1
      while(i >= 1 && j >= valueFirstIndex) {
        finalBuf(i) = str(j)
        i -= 1
        j -= 1
      }
      i = sv.length + sv.power + 1
      j = indexOfDecimalPoint + 1
      while(i < sv.length && j < str.length) {
        finalBuf(i) = str(j)
        i += 1
        j += 1
      }
    }

    finalBuf(0) = (if(isNegative) {'-'} else {'+'}).toByte
    for(i <- 0 until finalBuf.length) {
      addr.setByte(i, finalBuf(i))
    }
  }

  private def getPackedIndexAndByte(dataLen: Int, index: Int, digit: Byte): (Int, Byte) = {
    val d = (digit - '0').toByte
    if(dataLen % 2 == 0) {
      if(index % 2 == 0) {
        (((index + 1) / 2).toInt, d)
      } else {
        (((index + 1) / 2).toInt, (d << 4).toByte)
      }
    } else {
      if(index % 2 == 0) {
        (index / 2, (d << 4).toByte)
      } else {
        (index / 2, d)
      }
    }
  }
  private def createCobolDataUnsignedNumberPd(sv: SQLVar, addr: CobolDataStorage, i: Int, str: scala.Array[Byte]): Unit = {
    val strStartIndex = if (str(0) == '+'.toByte || str(0) == '-'.toByte) {1} else {0}
    val strPointIndex = {
      val index = str.indexWhere(_ == '.'.toByte)
      if(index < 0) {str.length} else {index}
    }
    val dataPointIndex = sv.length + sv.power
    val realDataLength = (sv.length / 2).toInt + 1

    addr.memset(0, realDataLength)
    addr.setByte(realDataLength - 1, 0x0F.toByte)

    for(i <- 0 to sv.length - 1) {
      var strIndex = {
        val index = i - dataPointIndex + strPointIndex
        if(index >= strPointIndex) {index + 1} else {index}
      }
      val digit = if(strIndex >= strStartIndex && strIndex < str.length) {
        str(strIndex)
      } else {
        '0'.toByte
      }
      val (index, byteValue) = getPackedIndexAndByte(sv.length, i, digit)
      val b = addr.getByte(index)
      addr.setByte(index, (b | byteValue).toByte)
    }
  }

  private def createCobolDataSignedNumberPd(sv: SQLVar, addr: CobolDataStorage, i: Int, str: scala.Array[Byte]): Unit = {
    val strStartIndex = if (str(0) == '+'.toByte || str(0) == '-'.toByte) {1} else {0}
    val sign = if (str(0) == '-'.toByte) {-1} else {1}
    val strPointIndex = {
      val index = str.indexWhere(_ == '.'.toByte)
      if(index < 0) {str.length} else {index}
    }
    val dataPointIndex = sv.length + sv.power
    val realDataLength = (sv.length / 2).toInt + 1

    addr.memset(0, realDataLength)
    if(sign > 0) {
      addr.setByte(realDataLength - 1, 0x0c.toByte)
    } else {
      addr.setByte(realDataLength - 1, 0x0d.toByte)
    }

    for(i <- 0 to sv.length - 1) {
      var strIndex = {
        val index = i - dataPointIndex + strPointIndex
        if(index >= strPointIndex) {index + 1} else {index}
      }
      val digit = if(strIndex >= strStartIndex && strIndex < str.length) {
        str(strIndex)
      } else {
        '0'.toByte
      }
      val (index, byteValue) = getPackedIndexAndByte(sv.length, i, digit)
      val b = addr.getByte(index)
      addr.setByte(index, (b | byteValue).toByte)
    }
  }

  private def createCobolDataAlphanumeric(sv: SQLVar, addr: CobolDataStorage, i: Int, str: scala.Array[Byte]): Unit = {
    if(str.length >= sv.length) {
      addr.memcpy(str, sv.length)
    } else {
      addr.memset(' '.toByte, sv.length)
      addr.memcpy(str, str.length)
    }
  }

  private def createCobolDataJapanese(sv: SQLVar, addr: CobolDataStorage, i: Int, str: scala.Array[Byte]): Unit = {
    if(str.length >= sv.length * 2) {
      addr.memcpy(str, sv.length * 2)
    } else {
      addr.memset(' '.toByte, sv.length * 2)
      addr.memcpy(str, str.length)
    }
  }

  private def createCobolDataAlphanumericVarying(sv: SQLVar, addr: CobolDataStorage, i: Int, str: scala.Array[Byte]): Unit = {
    if(str.length >= sv.length) {
      addr.memcpy(str, sv.length)
    } else {
      addr.memset(' '.toByte, sv.length)
      addr.memcpy(str, str.length)
    }
  }

  private def createCobolDataJapaneseVarying(sv: SQLVar, addr: CobolDataStorage, i: Int, str: scala.Array[Byte]): Unit = {
    //TODO Implement
  }

  private def sqlStateToSqlCode(state: String): Int = state match {
    case "02000" => OCPG_NOT_FOUND
    case "YE002" => OCPG_EMPTY
    case "08001" => OCPG_CONNECT
    case "08003" => OCPG_CONNECT
    case "08007" => OCPG_TRANS
    case "21000" => OCPG_SUBSELECT_NOT_ONE
    case "23505" => OCPG_DUPLICATE_KEY
    case "25001" => OCPG_WARNING_IN_TRANSACTION
    case "25P01" => OCPG_WARNING_NO_TRANSACTION
    case "34000" => OCPG_WARNING_UNKNOWN_PORTAL
    case "42804" => OCPG_DATA_FORMAT_ERROR
    case "42P03" => OCPG_WARNING_PORTAL_EXISTS
    case "55P03" => OCPG_PGSQL

    case _ => OCDB_UNKNOWN_ERROR
  }

}