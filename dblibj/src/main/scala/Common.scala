import ConstValues._
import Cursor._
import Operation._

import java.sql._
import ConnectionInfo.ConnectionMap
import jp.osscons.opensourcecobol.libcobj.data.CobolDataStorage

import java.nio.charset.Charset
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets
import scala.collection.immutable.Queue
import scala.runtime.Statics
import org.postgresql.util.PSQLException

object Common {
  var internalState: GlobalState = GlobalState.initialGlobalState

  def ocesqlExec(id: Int, query: Option[String], state: OCDBState): Unit = {
    state.initSqlca()
    query match {
      case None                 => setLibErrorStatus(OCDB_EMPTY(), state)
      case Some(q) if q.isEmpty => setLibErrorStatus(OCDB_EMPTY(), state)
      case Some(q) => {
        ocdbExec(id, q, state)
        val success = setResultStatus(id, state)
        if (success && (q == "COMMIT" || q == "ROLLBACK")) {
          clearCursorMap(id, state)
          ocdbExec(id, "BEGIN", state)
          setResultStatus(id, state)
        }
      }
    }
  }

  def ocesqlExecWhereCurrentOf(
      id: Int,
      query: Option[String],
      cursorName: Option[String],
      state: OCDBState
  ): Unit = {
    state.initSqlca()
    query match {
      case None                 => setLibErrorStatus(OCDB_EMPTY(), state)
      case Some(q) if q.isEmpty => setLibErrorStatus(OCDB_EMPTY(), state)
      case Some(q) =>
        cursorName match {
          case None => setLibErrorStatus(OCDB_EMPTY(), state)
          case Some(cursor) => {
            val cursorMap = state.globalState.cursorMap
            cursorMap.get(cursor) match {
              case None => setLibErrorStatus(OCDB_EMPTY(), state)
              case Some(c) =>
                execFetchWhereCurrentOf(c, cursor, cursorMap, id, query, state)
            }
          }
        }
    }
  }

  private def execFetchWhereCurrentOf(
      c: Cursor,
      cursor: String,
      cursorMap: CursorMap,
      id: Int,
      query: Option[String],
      state: OCDBState
  ): Unit =
    c.fetchRecords match {
      case Nil => {
        if (c.overFetch) {
          ocesqlExec(
            id,
            Some(s"FETCH BACKWARD 1 from ${cursor}"),
            state
          )
        }
        ocesqlExec(id, query, state)
      }
      case _ => {
        val fetchSize = if (c.overFetch) {
          c.fetchRecords.size + 1
        } else {
          c.fetchRecords.size
        }
        ocesqlExec(
          id,
          Some(
            s"FETCH BACKWARD ${fetchSize} from ${cursor}"
          ),
          state
        )
        ocesqlExec(id, query, state)
        val newCursor = c.setFetchRecords(Nil)
        val newCursorMap = cursorMap ++ Map(cursor -> newCursor)
        val newGlobalState =
          state.globalState.setCursorMap(newCursorMap)
        state.updateGlobalState(newGlobalState)
      }
    }

  def ocesqlExecParams(
      id: Int,
      query: Option[String],
      nParams: Int,
      state: OCDBState
  ): Unit = {
    state.initSqlca()

    if (query.getOrElse("").length == 0 || nParams == 0) {
      setLibErrorStatus(OCDB_EMPTY(), state)
      return ()
    }

    if (state.globalState.sqlVarQueue.size > nParams) {
      setLibErrorStatus(OCDB_TOO_MANY_ARGUMENTS(), state)
      return ()
    }

    if (state.globalState.sqlVarQueue.size < nParams) {
      setLibErrorStatus(OCDB_TOO_FEW_ARGUMENTS(), state)
      return ()
    }

    ocdbExecParams(
      id,
      query.getOrElse(""),
      state.globalState.sqlVarQueue,
      state
    )
    val result = setResultStatus(id, state)

    if (
      result && (query
        .getOrElse("") == "COMMIT" || query.getOrElse("") == "ROLLBACK")
    ) {
      clearCursorMap(id, state)
      ocdbExec(id, "BEGIN", state)
      setResultStatus(id, state)
    }
  }

  def ocesqlExecParamsWhereCurrentOf(
      id: Int,
      query: Option[String],
      nParams: Int,
      cursorName: Option[String],
      state: OCDBState
  ): Unit = {
    state.initSqlca()
    query match {
      case None                 => setLibErrorStatus(OCDB_EMPTY(), state)
      case Some(q) if q.isEmpty => setLibErrorStatus(OCDB_EMPTY(), state)
      case Some(q) =>
        cursorName match {
          case None => setLibErrorStatus(OCDB_EMPTY(), state)
          case Some(cursor) => {
            val cursorMap = state.globalState.cursorMap
            cursorMap.get(cursor) match {
              case None =>
                setLibErrorStatus(OCDB_EMPTY(), state)
              case Some(c) =>
                c.fetchRecords match {
                  case Nil => {
                    if (c.overFetch) {
                      ocesqlExec(
                        id,
                        Some(s"FETCH BACKWARD 1 from ${cursor}"),
                        state
                      )
                    }
                    ocesqlExecParams(id, query, nParams, state)
                  }
                  case _ => {
                    ocesqlExec(
                      id,
                      Some(
                        s"FETCH BACKWARD ${(if (c.overFetch) {
                                              c.fetchRecords.size + 1
                                            } else { c.fetchRecords.size })} from ${cursor}"
                      ),
                      state
                    )
                    ocesqlExecParams(id, query, nParams, state)
                    val newCursor = c.setFetchRecords(Nil)
                    val newCursorMap = cursorMap ++ Map(cursor -> newCursor)
                    val newGlobalState =
                      state.globalState.setCursorMap(newCursorMap)
                    state.updateGlobalState(newGlobalState)
                  }
                }
            }
          }
        }
    }
  }

  // [remark] 一部未実装
  def ocesqlExecParamsOccurs(
      id: Int,
      query: Option[String],
      nParams: Int,
      state: OCDBState
  ): Unit = {
    state.initSqlca()

    // [remark] 条件を正しく書く
    if (query.getOrElse("").length == 0 || nParams == 0 /*||*/ ) {
      setLibErrorStatus(OCDB_EMPTY(), state)
    }
  }

  def ocdbExec(id: Int, query: String, state: OCDBState): Unit = {
    val keyAndValue = lookUpConnList(id, state)
    if (keyAndValue.isEmpty) {
      return ()
    }

    val kv = keyAndValue.getOrElse("", ConnectionInfo.defaultValue)
    val key = kv._1
    val pConn = kv._2

    val result = ocdbPGExec(pConn.connAddr, query)
    setConnList(key, pConn.setResult(result), state)
  }

  def ocdbExecParams(
      id: Int,
      query: String,
      sqlVarQueue: Queue[SQLVar],
      state: OCDBState
  ): Unit =
    lookUpConnList(id, state) match {
      case None => logLn(s"id=${id},pConn=")
      case Some((key, pConn)) => {
        logLn(s"id=${id},pConn=${pConn}")
        val result = ocdbPGExecParam(pConn.connAddr, query, sqlVarQueue)
        val globalState = state.globalState
        val newPConn = pConn.setResult(Right(ESuccess())).setResult(result)
        val newConnMap = globalState.connectionMap + (key -> newPConn)
        val newGlobalState = globalState.setConnectionMap(newConnMap)
        state.updateGlobalState(newGlobalState)
        if (result.isLeft) {
          errorLogLn("PostgreSQL Result is null")
        }
      }
    }

  /** Equivalent to ocdbPGExec in dblib/ocpgsql.c [remark]
    * 決して実行されないと思われる処理を書いていない
    * @param connAddr
    * @param query
    * @return
    */
  def ocdbPGExec(connAddr: Option[Connection], query: String): ExecResult = {
    logLn(s"CONNADDR ${connAddr}, EXEC SQL ${query}")
    connAddr match {
      case None => Left(new SQLException())
      case Some(c) =>
        try {
          val stmt = c.createStatement()
          if (stmt.execute(query)) {
            Right(EResultSet(stmt.getResultSet()))
          } else {
            Right(EUpdateCount(stmt.getUpdateCount()))
          }
        } catch {
          case e: PSQLException => Left(e)
          case e: SQLException  => Left(e)
          case e: Throwable     => Left(new SQLException())
        }
    }
  }

  def ocdbFinish(id: Int, state: OCDBState): Unit =
    lookUpConnList(id, state) match {
      case None => ()
      case Some((key, pConn)) => {
        ocdbPGFinish(pConn.connAddr, state)
        logLn(s"connection id ${id} released.")
        freeConnLists(id, state)
      }
    }

  def ocdbPGFinish(conn: Option[Connection], state: OCDBState): Unit =
    conn match {
      case None    => ()
      case Some(c) => c.close()
    }

  // [remark] rollBakckOneModeに関連した処理の実装
  def ocdbPGExecParam(
      connAddr: Option[Connection],
      query: String,
      params: Queue[SQLVar]
  ): ExecResult = {
    logLn(s"CONNADDR: ${connAddr}, EXEC SQL: ${query}")
    connAddr match {
      case None => Left(new SQLException())
      case Some(c) =>
        try {
          val stmt = c.prepareStatement(query)
          val metaData: ParameterMetaData =
            PreparedStatementCache.getParameterMetaDataFromCache(query, stmt)
          for ((param, i) <- params.zipWithIndex) {
            param.setParam(stmt, i + 1, metaData)
          }
          if (stmt.execute()) {
            Right(EResultSet(stmt.getResultSet()))
          } else {
            Right(EUpdateCount(stmt.getUpdateCount()))
          }
        } catch {
          case e: PSQLException => Left(e)
          case e: SQLException  => Left(e)
          case e: Throwable     => Left(new SQLException())
        }
    }
  }

  /** Equivalent to look_up_conn_lists in dblib/ocpgsql.c
    * @param id
    * @return
    */
  def lookUpConnList(
      id: Int,
      state: OCDBState
  ): Option[(String, ConnectionInfo)] = {
    val connMap = state.globalState.connectionMap
    val filteredList = connMap.filter(t => t._2.id == id).toList
    filteredList match {
      case Nil    => None
      case x :: _ => Some((x._1, x._2))
    }
  }

  def updateConnList(
      id: Int,
      newConn: ConnectionInfo,
      state: OCDBState
  ): Unit = {
    val connectionMap = state.globalState.connectionMap
    val newConnectionMap = connectionMap.transform((_, conn) =>
      if (conn.id == id) { newConn }
      else { conn }
    )
    val newGlobalState = state.globalState.setConnectionMap(newConnectionMap)
    state.updateGlobalState(newGlobalState)
  }

  def updateConnList(
      key: String,
      f: ConnectionInfo => ConnectionInfo,
      state: OCDBState
  ): Unit = {
    val connectionMap = state.globalState.connectionMap
    val connection = connectionMap.get(key)
    connection match {
      case None => ()
      case Some(c) => {
        val newConnectionMap = connectionMap.updated(key, f(c))
        val newGlobalState =
          state.globalState.setConnectionMap(newConnectionMap)
        state.updateGlobalState(newGlobalState)
      }
    }
  }

  def freeConnLists(id: Int, state: OCDBState): Unit = {
    val connectionMap = state.globalState.connectionMap
    val newConnectionMap = connectionMap.filter(_._2.id != id)
    val newGlobalState = state.globalState.setConnectionMap(newConnectionMap)
    state.updateGlobalState(newGlobalState)
  }

  def setConnList(key: String, c: ConnectionInfo, state: OCDBState): Unit =
    updateConnList(key, (_ => c), state)

  def setLibErrorStatus(errorCode: SqlCode, state: OCDBState): Int =
    ocdbPGsetLibErrorStatus(errorCode, state)

  def setResultStatus(id: Int, state: OCDBState): Boolean =
    lookUpConnList(id, state) match {
      case None             => false
      case Some((_, pConn)) => ocdbPGsetResultStatus(pConn.result, state)
    }

  def ocdbPGsetResultStatus(result: ExecResult, state: OCDBState): Boolean = {
    val (sqlCode, sqlState) = result match {
      case Right(_) => {
        (OCPG_NO_ERROR, "00000")
      }
      case Left(e) => {
        val sqlState = Option(e.getSQLState).getOrElse("     ")
        (sqlStateToSqlCode(sqlState), sqlState)
      }
    }

    val errMsgInfo = result match {
      case Left(e) =>
        Option(e.getMessage) match {
          case Some(msg) => {
            val bytes = msg.getBytes
            Some((bytes, bytes.length))
          }
          case _ => None
        }
      case _ => None
    }

    // Output Log
    errMsgInfo match {
      case Some((msg, _)) => {
        logLn(s"MESSAGE:${msg}")
        errorLogLn(String.format("%d:%5s:%-70s", sqlCode, sqlState, msg))
      }
      case _ => ()
    }

    // Update SQLCA
    val sqlCA = state.sqlCA
    val tmpSqlCA = sqlCA.setCode(sqlCode).setState(sqlState.getBytes)
    val newSqlCA = errMsgInfo match {
      case Some((msg, len)) =>
        tmpSqlCA
          .setErrmc(msg)
          .setErrml(len.toShort)
      case _ => tmpSqlCA
    }
    state.updateSQLCA(newSqlCA)

    return result.isRight
  }

  /** Implementation of ocdbPGsetResultStatus in dblib/ocpgsql.c
    * (Open-COBOL-ESQL) Regardless of the pretious SQL execution, sqlErrorCode
    * is zero. If the pretious SQL execution fails, sqlState equals to
    * PSQLState.
    * (https://github.com/pgjdbc/pgjdbc/blob/8be516d47ece60b7aeba5a9474b5cac1d538a04a/pgjdbc/src/main/java/org/postgresql/util/PSQLState.java)
    * @param connAddr
    * @return
    *   true if and only if the previous SQL execution is successful.
    */

  def resolveCONNID(
      cid: String,
      connectionMap: ConnectionMap
  ): Option[ConnectionInfo] =
    connectionMap.get(cid) match {
      case None => None
      case Some(info) => {
        logLn(s"return connid=${cid}")
        Some(info)
      }
    }

  def resolveCONNID(cid: String, state: OCDBState): Option[ConnectionInfo] = {
    Common.resolveCONNID(cid, state.globalState.connectionMap)
  }
  // scalastyle:off method.length
  // scalastyle:off cyclomatic.complexity
  def ocdbPGsetLibErrorStatus(errorCode: SqlCode, state: OCDBState): Int = {
    val (code, sqlState) = errorCode match {
      case OCDB_NO_ERROR()           => (OCPG_NO_ERROR, "00000")
      case OCDB_NOT_FOUND()          => (OCPG_NOT_FOUND, "02000")
      case OCDB_OUT_OF_MEMORY()      => (OCPG_OUT_OF_MEMORY, "YE001")
      case OCDB_UNSUPPORTED()        => (OCPG_UNSUPPORTED, "YE002")
      case OCDB_TOO_MANY_ARGUMENTS() => (OCPG_TOO_MANY_ARGUMENTS, "07001")
      case OCDB_TOO_FEW_ARGUMENTS()  => (OCPG_TOO_FEW_ARGUMENTS, "07002")
      case OCDB_TOO_MANY_MATCHES()   => (OCPG_TOO_MANY_MATCHES, "21000")
      case OCDB_DATA_FORMAT_ERROR()  => (OCPG_DATA_FORMAT_ERROR, "42804")
      case OCDB_EMPTY()              => (OCPG_EMPTY, "YE002")
      case OCDB_MISSING_INDICATOR()  => (OCPG_MISSING_INDICATOR, "22002")
      case OCDB_NO_CONN()            => (OCPG_NO_CONN, "08003")
      case OCDB_NOT_CONN()           => (OCPG_NOT_CONN, "YE002")
      case OCDB_INVALID_STMT()       => (OCPG_INVALID_STMT, "26000")
      case OCDB_INFORMIX_DUPLICATE_KEY() =>
        (OCPG_INFORMIX_DUPLICATE_KEY, "23505")
      case OCDB_UNKNOWN_DESCRIPTOR() => (OCPG_UNKNOWN_DESCRIPTOR, "33000")
      case OCDB_INVALID_DESCRIPTOR_INDEX() =>
        (OCPG_INVALID_DESCRIPTOR_INDEX, "07009")
      case OCDB_UNKNOWN_DESCRIPTOR_ITEM() =>
        (OCPG_UNKNOWN_DESCRIPTOR_ITEM, "YE002")
      case OCDB_VAR_NOT_NUMERIC() => (OCPG_VAR_NOT_NUMERIC, "07006")
      case OCDB_VAR_NOT_CHAR()    => (OCPG_VAR_NOT_CHAR, "07006")
      case OCDB_INFORMIX_SUBSELECT_NOT_ONE() =>
        (OCPG_INFORMIX_SUBSELECT_NOT_ONE, "21000")
      case OCDB_PGSQL()             => (OCPG_PGSQL, "     ")
      case OCDB_TRANS()             => (OCPG_TRANS, "08007")
      case OCDB_CONNECT()           => (OCPG_CONNECT, "08001")
      case OCDB_DUPLICATE_KEY()     => (OCPG_DUPLICATE_KEY, "23505")
      case OCDB_SUBSELECT_NOT_ONE() => (OCPG_SUBSELECT_NOT_ONE, "21000")
      case OCDB_WARNING_UNKNOWN_PORTAL() =>
        (OCPG_WARNING_UNKNOWN_PORTAL, "34000")
      case OCDB_WARNING_IN_TRANSACTION() =>
        (OCPG_WARNING_IN_TRANSACTION, "25001")
      case OCDB_WARNING_NO_TRANSACTION() =>
        (OCPG_WARNING_NO_TRANSACTION, "25P01")
      case OCDB_WARNING_PORTAL_EXISTS() => (OCPG_WARNING_PORTAL_EXISTS, "42P03")
      case OCDB_LOCK_ERROR()            => (OCPG_LOCK_ERROR, "57033")
      case OCDB_JDD_ERROR()             => (OCPG_JDD_ERROR, "     ")
      case _                            => (OCDB_UNKNOWN_ERROR, "     ")
    }

    val sqlCA = state.sqlCA
    val newSqlCA = if (code == OCPG_PGSQL) {
      sqlCA.setCode(code)
    } else {
      sqlCA.setCode(code).setState(sqlState.getBytes())
    }
    state.updateSQLCA(newSqlCA)
    if (code < 0) {
      RESULT_ERROR
    } else {
      RESULT_SUCCESS
    }
  }
  // scalastyle:on method.length
  // scalastyle:on cyclomatic.complexity

  def getCString(storage: CobolDataStorage): Option[String] = {
    try {
      var i = 0
      while (storage.getByte(i) != 0) {
        i += 1
      }
      Some(new String(storage.getByteArray(0, i), Charset.forName("SHIFT-JIS")))
    } catch {
      case e: Throwable => None
    }
  }

  def getCStringLength(storage: CobolDataStorage): Int = {
    try {
      var i = 0
      while (storage.getByte(i) != 0) {
        i += 1
      }
      i
    } catch {
      case e: Throwable => 0
    }
  }

  // inpure function
  def createCobolDataLowValue(
      sv: SQLVar,
      index: Int,
      occursInfo: OccursInfo
  ): Unit = {
    val addr = if (occursInfo.isPresent) {
      sv.addr
        .getOrElse(nullDataStorage)
        .getSubDataStorage(index * occursInfo.length)
    } else {
      sv.addr.getOrElse(nullDataStorage).getSubDataStorage(index * sv.length)
    }
    addr.memset(0, sv.length)
  }

  // inpure function
  def createCobolData(
      sv: SQLVar,
      index: Int,
      resultData: scala.Array[Byte],
      occursInfo: OccursInfo
  ): Unit = {
    val realLength =
      if (occursInfo.isPresent) {
        occursInfo.length
      } else {
        sv.length
      }

    val addr =
      sv.addr.getOrElse(nullDataStorage).getSubDataStorage(index * realLength)
    sv.sqlVarType match {
      case OCDB_TYPE_UNSIGNED_NUMBER =>
        createCobolDataUnsignedNumber(sv, addr, index, resultData)
      case OCDB_TYPE_SIGNED_NUMBER_TC =>
        createCobolDataSignedNumberTc(sv, addr, index, resultData)
      case OCDB_TYPE_SIGNED_NUMBER_LS =>
        createCobolDataSignedNumberLs(sv, addr, index, resultData)
      case OCDB_TYPE_UNSIGNED_NUMBER_PD =>
        createCobolDataUnsignedNumberPd(sv, addr, index, resultData)
      case OCDB_TYPE_SIGNED_NUMBER_PD =>
        createCobolDataSignedNumberPd(sv, addr, index, resultData)
      case OCDB_TYPE_ALPHANUMERIC =>
        createCobolDataAlphanumeric(sv, addr, index, resultData)
      case OCDB_TYPE_JAPANESE =>
        createCobolDataJapanese(sv, addr, index, resultData)
      case OCDB_TYPE_ALPHANUMERIC_VARYING =>
        createCobolDataAlphanumericVarying(sv, addr, index, resultData)
      case OCDB_TYPE_JAPANESE_VARYING =>
        createCobolDataJapaneseVarying(sv, addr, index, resultData)
    }
  }

  // [TODO] improve the algorithm
  private def createCobolDataUnsignedNumber(
      sv: SQLVar,
      addr: CobolDataStorage,
      index: Int,
      str: scala.Array[Byte]
  ): Unit = {
    val finalBuf: scala.Array[Byte] = new scala.Array(sv.length)
    val isNegative = str(0) == '-'.toByte
    val valueFirstIndex = if (isNegative) { 1 }
    else { 0 }
    val indexOfDecimalPoint = {
      val index = str.indexOf('.')
      if (index < 0) { str.length }
      else { index }
    }

    for (i <- 0 until finalBuf.length) {
      finalBuf(i) = '0'.toByte
    }

    if (sv.power >= 0) {
      for (i <- valueFirstIndex until indexOfDecimalPoint) {
        finalBuf(i + finalBuf.length - (indexOfDecimalPoint + sv.power)) = str(
          i
        )
      }
    } else {
      var i = sv.length + sv.power - 1
      var j = indexOfDecimalPoint - 1
      while (i >= 0 && j >= valueFirstIndex) {
        finalBuf(i) = str(j)
        i -= 1
        j -= 1
      }
      i = sv.length + sv.power
      j = indexOfDecimalPoint + 1
      while (i < sv.length && j < str.length) {
        finalBuf(i) = str(j)
        i += 1
        j += 1
      }
    }

    for (i <- 0 until finalBuf.length) {
      addr.setByte(i, finalBuf(i))
    }
  }

  private def createCobolDataSignedNumberTc(
      sv: SQLVar,
      addr: CobolDataStorage,
      index: Int,
      str: scala.Array[Byte]
  ): Unit = {
    val finalBuf: scala.Array[Byte] = new scala.Array(sv.length)
    val isNegative = str(0) == '-'.toByte
    val valueFirstIndex = if (isNegative) { 1 }
    else { 0 }
    val indexOfDecimalPoint = {
      val index = str.indexOf('.')
      if (index < 0) { str.length }
      else { index }
    }

    for (i <- 0 until finalBuf.length) {
      finalBuf(i) = '0'.toByte
    }

    if (sv.power >= 0) {
      for (i <- valueFirstIndex until indexOfDecimalPoint) {
        finalBuf(i + finalBuf.length - (indexOfDecimalPoint + sv.power)) = str(
          i
        )
      }
    } else {
      var i = sv.length + sv.power - 1
      var j = indexOfDecimalPoint - 1
      while (i >= 0 && j >= valueFirstIndex) {
        finalBuf(i) = str(j)
        i -= 1
        j -= 1
      }
      i = sv.length + sv.power
      j = indexOfDecimalPoint + 1
      while (i < sv.length && j < str.length) {
        finalBuf(i) = str(j)
        i += 1
        j += 1
      }
    }

    if (isNegative) {
      val finalByte = finalBuf(finalBuf.length - 1)
      finalBuf(finalBuf.length - 1) = (finalByte + 0x40).toByte
    }
    for (i <- 0 until finalBuf.length) {
      addr.setByte(i, finalBuf(i))
    }
  }

  private def createCobolDataSignedNumberLs(
      sv: SQLVar,
      addr: CobolDataStorage,
      i: Int,
      str: scala.Array[Byte]
  ): Unit = {
    val finalBuf: scala.Array[Byte] = new scala.Array(sv.length)
    val isNegative = str(0) == '-'.toByte
    val valueFirstIndex = if (isNegative) { 1 }
    else { 0 }
    val indexOfDecimalPoint = {
      val index = str.indexOf('.')
      if (index < 0) { str.length }
      else { index }
    }

    for (i <- 0 until finalBuf.length) {
      finalBuf(i) = '0'.toByte
    }

    if (sv.power >= 0) {
      for (i <- valueFirstIndex until indexOfDecimalPoint) {
        finalBuf(i + finalBuf.length - (indexOfDecimalPoint + sv.power)) = str(
          i
        )
      }
    } else {
      var i = sv.length + sv.power
      var j = indexOfDecimalPoint - 1
      while (i >= 1 && j >= valueFirstIndex) {
        finalBuf(i) = str(j)
        i -= 1
        j -= 1
      }
      i = sv.length + sv.power + 1
      j = indexOfDecimalPoint + 1
      while (i < sv.length && j < str.length) {
        finalBuf(i) = str(j)
        i += 1
        j += 1
      }
    }

    finalBuf(0) = (if (isNegative) { '-' }
                   else { '+' }).toByte
    for (i <- 0 until finalBuf.length) {
      addr.setByte(i, finalBuf(i))
    }
  }

  private def getPackedIndexAndByte(
      dataLen: Int,
      index: Int,
      digit: Byte
  ): (Int, Byte) = {
    val d = (digit - '0').toByte
    if (dataLen % 2 == 0) {
      if (index % 2 == 0) {
        (((index + 1) / 2).toInt, d)
      } else {
        (((index + 1) / 2).toInt, (d << 4).toByte)
      }
    } else {
      if (index % 2 == 0) {
        (index / 2, (d << 4).toByte)
      } else {
        (index / 2, d)
      }
    }
  }
  private def createCobolDataUnsignedNumberPd(
      sv: SQLVar,
      addr: CobolDataStorage,
      i: Int,
      str: scala.Array[Byte]
  ): Unit = {
    val strStartIndex = if (str(0) == '+'.toByte || str(0) == '-'.toByte) { 1 }
    else { 0 }
    val strPointIndex = {
      val index = str.indexWhere(_ == '.'.toByte)
      if (index < 0) { str.length }
      else { index }
    }
    val dataPointIndex = sv.length + sv.power
    val realDataLength = (sv.length / 2).toInt + 1

    addr.memset(0, realDataLength)
    addr.setByte(realDataLength - 1, 0x0f.toByte)

    for (i <- 0 to sv.length - 1) {
      var strIndex = {
        val index = i - dataPointIndex + strPointIndex
        if (index >= strPointIndex) { index + 1 }
        else { index }
      }
      val digit = if (strIndex >= strStartIndex && strIndex < str.length) {
        str(strIndex)
      } else {
        '0'.toByte
      }
      val (index, byteValue) = getPackedIndexAndByte(sv.length, i, digit)
      val b = addr.getByte(index)
      addr.setByte(index, (b | byteValue).toByte)
    }
  }

  private def createCobolDataSignedNumberPd(
      sv: SQLVar,
      addr: CobolDataStorage,
      i: Int,
      str: scala.Array[Byte]
  ): Unit = {
    val strStartIndex = if (str(0) == '+'.toByte || str(0) == '-'.toByte) { 1 }
    else { 0 }
    val sign = if (str(0) == '-'.toByte) { -1 }
    else { 1 }
    val strPointIndex = {
      val index = str.indexWhere(_ == '.'.toByte)
      if (index < 0) { str.length }
      else { index }
    }
    val dataPointIndex = sv.length + sv.power
    val realDataLength = (sv.length / 2).toInt + 1

    addr.memset(0, realDataLength)
    if (sign > 0) {
      addr.setByte(realDataLength - 1, 0x0c.toByte)
    } else {
      addr.setByte(realDataLength - 1, 0x0d.toByte)
    }

    for (i <- 0 to sv.length - 1) {
      var strIndex = {
        val index = i - dataPointIndex + strPointIndex
        if (index >= strPointIndex) { index + 1 }
        else { index }
      }
      val digit = if (strIndex >= strStartIndex && strIndex < str.length) {
        str(strIndex)
      } else {
        '0'.toByte
      }
      val (index, byteValue) = getPackedIndexAndByte(sv.length, i, digit)
      val b = addr.getByte(index)
      addr.setByte(index, (b | byteValue).toByte)
    }
  }

  private def createCobolDataAlphanumeric(
      sv: SQLVar,
      addr: CobolDataStorage,
      i: Int,
      str: scala.Array[Byte]
  ): Unit = {
    if (str.length >= sv.length) {
      addr.memcpy(str, sv.length)
    } else {
      addr.memset(' '.toByte, sv.length)
      addr.memcpy(str, str.length)
    }
  }

  private def createCobolDataJapanese(
      sv: SQLVar,
      addr: CobolDataStorage,
      i: Int,
      str: scala.Array[Byte]
  ): Unit = {
    for (j <- 0 to sv.length - 1) {
      addr.setByte(j * 2, 0x30.toByte)
      addr.setByte(j * 2 + 1, 0x00.toByte)
    }
    addr.memcpy(str, Math.min(sv.length * 2, str.length))
  }

  private def createCobolDataAlphanumericVarying(
      sv: SQLVar,
      addr: CobolDataStorage,
      i: Int,
      str: scala.Array[Byte]
  ): Unit = {
    if (str.length >= sv.length) {
      addr.memcpy(str, sv.length)
    } else {
      addr.memset(' '.toByte, sv.length)
      addr.memcpy(str, str.length)
    }
  }

  private def createCobolDataJapaneseVarying(
      sv: SQLVar,
      addr: CobolDataStorage,
      i: Int,
      str: scala.Array[Byte]
  ): Unit = {
    // TODO Implement
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
