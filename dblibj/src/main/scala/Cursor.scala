import Operation._
import Common._
import ConstValues._
import scala.collection.immutable.Queue
import java.sql.Connection
import Prepare._
import java.sql.ResultSet
import org.postgresql.util.PSQLException

class Cursor(
    val connId: Int,
    val name: String,
    val sp: List[QueryInfo],
    val query: String,
    val nParams: Int,
    val isOpened: Boolean,
    val tuples: Int,
    val sqlVarQueue: Queue[SQLVar], // pList
    val fetchRecords: List[List[Option[Array[Byte]]]],
    val overFetch: Boolean
) {
  def setConnId(connId: Int): Cursor = new Cursor(
    connId,
    name,
    sp,
    query,
    nParams,
    isOpened,
    tuples,
    sqlVarQueue,
    fetchRecords,
    overFetch
  )
  def setName(name: String): Cursor = new Cursor(
    connId,
    name,
    sp,
    query,
    nParams,
    isOpened,
    tuples,
    sqlVarQueue,
    fetchRecords,
    overFetch
  )
  def setSp(sp: List[QueryInfo]): Cursor = new Cursor(
    connId,
    name,
    sp,
    query,
    nParams,
    isOpened,
    tuples,
    sqlVarQueue,
    fetchRecords,
    overFetch
  )
  def setQuery(query: String): Cursor = new Cursor(
    connId,
    name,
    sp,
    query,
    nParams,
    isOpened,
    tuples,
    sqlVarQueue,
    fetchRecords,
    overFetch
  )
  def setNParams(nParams: Int): Cursor = new Cursor(
    connId,
    name,
    sp,
    query,
    nParams,
    isOpened,
    tuples,
    sqlVarQueue,
    fetchRecords,
    overFetch
  )
  def setIsOpened(isOpened: Boolean): Cursor = new Cursor(
    connId,
    name,
    sp,
    query,
    nParams,
    isOpened,
    tuples,
    sqlVarQueue,
    fetchRecords,
    overFetch
  )
  def setTuples(tuples: Int): Cursor = new Cursor(
    connId,
    name,
    sp,
    query,
    nParams,
    isOpened,
    tuples,
    sqlVarQueue,
    fetchRecords,
    overFetch
  )
  def setSqlVarQueue(sqlVarQueue: Queue[SQLVar]): Cursor = new Cursor(
    connId,
    name,
    sp,
    query,
    nParams,
    isOpened,
    tuples,
    sqlVarQueue,
    fetchRecords,
    overFetch
  )
  def setFetchRecords(fetchRecords: List[List[Option[Array[Byte]]]]): Cursor =
    new Cursor(
      connId,
      name,
      sp,
      query,
      nParams,
      isOpened,
      tuples,
      sqlVarQueue,
      fetchRecords,
      overFetch
    )
  def setOverFetch(overFetch: Boolean): Cursor = new Cursor(
    connId,
    name,
    sp,
    query,
    nParams,
    isOpened,
    tuples,
    sqlVarQueue,
    fetchRecords,
    overFetch
  )
}

object Cursor {
  type CursorMap = scala.collection.immutable.Map[String, Cursor]
  type QueryInfoMap = scala.collection.immutable.Map[String, QueryInfo]
  def defaultValue(): Cursor =
    new Cursor(0, "", Nil, "", 0, false, 0, Queue(), Nil, false)
  def emptyCursorMap: CursorMap = Map.empty

  def clearCursorMap(id: Int, state: OCDBState): Unit = {
    val globalState = state.globalState
    val cursorMap = globalState.cursorMap
    val newCursorMap = cursorMap.transform((_, cursor) =>
      if (cursor.connId == id) {
        cursor.setIsOpened(false)
      } else {
        cursor
      }
    )
    val newGlobalState = globalState.setCursorMap(newCursorMap)
    state.updateGlobalState(newGlobalState)
  }

  def getCursorFromMap(cname: String, state: OCDBState): Option[Cursor] = {
    logLn(s"target:${cname}")
    state.globalState.cursorMap.get(cname) match {
      case None => {
        errorLogLn(s"cursor name '${cname}' is not found in cursor list.")
        None
      }
      case Some(c) => {
        logLn(s"#return:${c.name}#")
        Some(c)
      }
    }
  }

  def updateCursorMap(cname: String, cursor: Cursor, state: OCDBState): Unit = {
    val newCursorMap = state.globalState.cursorMap + (cname -> cursor)
    val newGlobalState = state.globalState.setCursorMap(newCursorMap)
    state.updateGlobalState(newGlobalState)
  }

  def updateFetchRecords(
      cursorName: String,
      fetchRecords: List[List[Option[Array[Byte]]]],
      updateOverFetch: Boolean,
      state: OCDBState
  ): Unit = {
    state.globalState.cursorMap.get(cursorName) match {
      case None => ()
      case Some(cursor) => {
        val newCursor = if (updateOverFetch) {
          cursor
            .setFetchRecords(fetchRecords)
            .setOverFetch(
              0 < fetchRecords.size && fetchRecords.size < GlobalState.fetch_records
            )
        } else {
          cursor.setFetchRecords(fetchRecords)
        }
        val newCursorMap =
          state.globalState.cursorMap ++ Map(cursorName -> newCursor)
        val newGlobalState = state.globalState.setCursorMap(newCursorMap)
        state.updateGlobalState(newGlobalState)
      }
    }
  }

  def addCursorMap(
      id: Int,
      cname: String,
      query: String,
      nParams: Int,
      state: OCDBState
  ): Boolean = {
    state.globalState.cursorMap.get(cname) match {
      case Some(cursor) if (cursor.isOpened) => {
        errorLogLn(s"cursor name '${cname}' alrready registred and opend")
        false
      }
      case _ => {
        val globalState = state.globalState
        val sqlVarQueue = globalState.sqlVarQueue
        val newCursor = Cursor
          .defaultValue()
          .setName(cname)
          .setConnId(id)
          .setQuery(query)
          .setNParams(nParams)
          .setSqlVarQueue(if (nParams > 0) { sqlVarQueue }
          else { Queue() })
          .setIsOpened(false)
          .setTuples(0)
        val newCursorMap = globalState.cursorMap + (cname -> newCursor)
        val newGlobalState = globalState.setCursorMap(newCursorMap)
        state.updateGlobalState(newGlobalState)
        true
      }
    }
  }

  def addCursorMapWithPrepare(
      id: Int,
      cname: String,
      prepare: QueryInfo,
      state: OCDBState
  ): Boolean = {
    state.globalState.cursorMap.get(cname) match {
      case Some(cursor) if (cursor.isOpened) => {
        errorLogLn(s"cursor name '${cname}' alrready registred and opend")
        false
      }
      case _ => {
        val globalState = state.globalState
        val newCursor = Cursor
          .defaultValue()
          .setName(cname)
          .setConnId(id)
          .setSp(List(prepare))
          .setIsOpened(false)
          .setTuples(0)
        val newCursorMap = globalState.cursorMap + (cname -> newCursor)
        val newGlobalState = globalState.setCursorMap(newCursorMap)
        state.updateGlobalState(newGlobalState)
        true
      }
    }
  }

  def ocesqlCursorDeclare(
      id: Int,
      cname: Option[String],
      query: Option[String],
      nParams: Int,
      state: OCDBState
  ): Unit = {
    val cname_ = cname.getOrElse("")
    val query_ = query.getOrElse("")
    state.updateSQLCA(SqlCA.defaultValue)
    if (cname_ == "" || query_ == "") {
      setLibErrorStatus(OCDB_EMPTY(), state)
    } else {
      val res = addCursorMap(id, cname_, query_, nParams, state)
      if (!res) {
        setLibErrorStatus(OCDB_WARNING_PORTAL_EXISTS(), state)
      }
    }
  }

  def ocesqlPreparedCursorDeclare(
      id: Int,
      cname: Option[String],
      sname: Option[String],
      state: OCDBState
  ): Unit = {
    val cname_ = cname.getOrElse("")
    val sname_ = sname.getOrElse("")
    state.initSqlca()
    if (cname_ == "" || sname_ == "") {
      setLibErrorStatus(OCDB_EMPTY(), state)
    } else {
      getPrepareFromMap(Some(sname_), state) match {
        case None => {
          errorLogLn(s"prepare ${sname_} not registered.")
          setLibErrorStatus(OCDB_INVALID_STMT(), state)
        }
        case Some(prepare) => {
          if (addCursorMapWithPrepare(id, cname_, prepare, state)) {
            setLibErrorStatus(OCDB_WARNING_PORTAL_EXISTS(), state)
          }
        }
      }
    }
  }

  def getPrepareFromMap(
      sname: Option[String],
      state: OCDBState
  ): Option[QueryInfo] = sname match {
    case None => None
    case Some(key) =>
      state.globalState.queryInfoMap.get(key) match {
        case None => {
          errorLogLn(s"prepare name '${sname}' is not found in prepare list.")
          showQueryInfoMap()
          None
        }
        case Some(queryInfo) => {
          logLn(s"#return:${queryInfo.pName}#")
          Some(queryInfo)
        }
      }
  }

  // TODO implement
  private def showQueryInfoMap(): Unit = ()
  private def ocesqlExecPrepareErrorProc(
      prepare: QueryInfo,
      nParams: Int,
      state: OCDBState
  ): Int = {
    errorLogLn(
      s"A number of parameters(${nParams}) and prepared sql parameters(${prepare.nParams} is unmatch.)"
    )
    setLibErrorStatus(OCDB_EMPTY(), state)
    val sqlCA = state.sqlCA
    val errorMessageBytes =
      "A number of paramteters and prepared sql parameters is unmatch"
        .getBytes()
    val newSqlCA = sqlCA
      .setErrmc(errorMessageBytes)
      .setErrml(errorMessageBytes.length.toShort)
    state.updateSQLCA(newSqlCA)
    1
  }

  private def ocesqlExecPrepareEndProc(
      id: Int,
      query: String,
      state: OCDBState
  ): Int = {
    if (setResultStatus(id, state)) {
      if (query == "COMMIT" || query == "ROLLBACK") {
        clearCursorMap(id, state)
        ocdbExec(id, "BEGIN", state)
      }
      0
    } else {
      1
    }
  }
  def ocesqlExecPrepare(
      id: Int,
      sname: Option[String],
      nParams: Int,
      state: OCDBState
  ): Int = {

    getPrepareFromMap(sname, state) match {
      case Some(prepare) if prepare.query.length != 0 =>
        if (nParams > 0) {
          if (prepare.nParams != nParams) {
            ocesqlExecPrepareErrorProc(prepare, nParams, state)
          } else {
            ocdbExecParams(
              id,
              prepare.query,
              state.globalState.sqlVarQueue,
              state
            )
            ocesqlExecPrepareEndProc(id, prepare.query, state)
          }
        } else {
          ocdbExec(id, prepare.query, state)
          ocesqlExecPrepareEndProc(id, prepare.query, state)
        }
      case _ => 1
    }
  }

  def ocdbCursorDeclare(
      id: Int,
      cname: String,
      query: String,
      withHold: Boolean,
      state: OCDBState
  ): Unit =
    lookUpConnList(id, state) match {
      case None => ()
      case Some((_, pConn)) => {
        val result =
          ocdbPGCursorDeclear(pConn.connAddr, cname, query, withHold, state)
        result match {
          case Right(EResultSet(rs)) =>
            updateConnList(id, pConn.setResult(result), state)
          case _ => {
            updateConnList(id, pConn.setResult(result), state)
            errorLogLn("PostgreSQL Result is NULL")
          }
        }
      }
    }

  def ocdbCursorDeclareParams(
      id: Int,
      cname: String,
      query: String,
      sqlVarQueue: Queue[SQLVar],
      withHold: Boolean,
      state: OCDBState
  ): Unit =
    lookUpConnList(id, state) match {
      case None => ()
      case Some((_, pConn)) => {
        val result = ocdbPGCursorDeclareParams(
          pConn.connAddr,
          cname,
          query,
          sqlVarQueue,
          withHold,
          state
        )
        result match {
          case Right(EResultSet(rs)) =>
            updateConnList(id, pConn.setResult(result), state)
          case _ => {
            updateConnList(id, pConn.setResult(result), state)
            errorLogLn("PostgreSQL Result is NULL")
          }
        }
      }
    }

  def ocdbPGCursorDeclear(
      conn: Option[Connection],
      cname: String,
      query: String,
      withHold: Boolean,
      state: OCDBState
  ): ExecResult = {
    val command = if (withHold == OCDB_CURSOR_WITH_HOLD_ON) {
      s"DECLARE ${cname} CURSOR WITH HOLD FOR ${query}"
    } else {
      s"DECLARE ${cname} CURSOR FOR ${query}"
    }
    val result = ocdbPGExec(conn, command)
    result match {
      case Left(e) =>
        logLn(e.getMessage())
      case _ =>
        logLn("declare cursor success")
    }
    result
  }

  def ocdbPGCursorDeclareParams(
      conn: Option[Connection],
      cname: String,
      query: String,
      sqlVarQueue: Queue[SQLVar],
      withHold: Boolean,
      state: OCDBState
  ): ExecResult = {
    val command = if (withHold == OCDB_CURSOR_WITH_HOLD_ON) {
      s"DECLARE ${cname} CURSOR WITH HOLD FOR ${query}"
    } else {
      s"DECLARE ${cname} CURSOR FOR ${query}"
    }
    val result = ocdbPGExecParam(conn, command, sqlVarQueue)
    result match {
      case Left(e) =>
        logLn(e.getMessage())
      case _ =>
        logLn("declare cursor success")
    }
    result
  }

  def ocdbCursorOpen(id: Int, cname: String, state: OCDBState): Unit =
    lookUpConnList(id, state) match {
      case None => ()
      case Some((_, pConn)) =>
        updateConnList(id, pConn.setResult(Right(ESuccess())), state)
    }

  def ocdbCursorFetchOccurs(
      id: Int,
      cname: String,
      fetchMode: ReadDirection,
      count: Int,
      state: OCDBState
  ): Unit =
    lookUpConnList(id, state) match {
      case None => ()
      case Some((_, pConn)) => {
        logLn(s"addr:${pConn.connAddr
            .getOrElse("")}, cname:${cname}, mode:${fetchMode}, count:${count}")
        ocdbPGCursorFetchOccurs(pConn, cname, fetchMode, count, state)
      }
    }

  def ocdbPGCursorFetchOccurs(
      conn: ConnectionInfo,
      cname: String,
      fetchMode: ReadDirection,
      count: Int,
      state: OCDBState
  ): Unit = {
    val strReadMode = fetchMode match {
      case OCDB_READ_PREVIOUS() => "BACKWARD"
      case _                    => "FORWARD"
    }
    ocdbExec(conn.id, s"FETCH ${strReadMode} ${count} FROM ${cname}", state)
  }

  def ocdbCursorFetchOne(
      id: Int,
      cname: String,
      fetchMode: ReadDirection,
      state: OCDBState
  ): Unit =
    lookUpConnList(id, state) match {
      case None => ()
      case Some((_, pConn)) => {
        logLn(
          s"addr:${pConn.connAddr.getOrElse("")}, cname:${cname}, mode:${fetchMode}"
        )
        ocdbPGCursorFetchOne(pConn, cname, fetchMode, state)
      }
    }

  def ocdbPGCursorFetchOne(
      conn: ConnectionInfo,
      cname: String,
      fetchMode: ReadDirection,
      state: OCDBState
  ): Unit = {
    fetchMode match {
      case OCDB_READ_PREVIOUS() =>
        ocdbExec(
          conn.id,
          s"FETCH BACKWARD ${GlobalState.getFetchRecords} FROM ${cname}",
          state
        )
      case OCDB_READ_CURRENT() =>
        ocdbExec(conn.id, s"FETCH FORWARD 0 FROM ${cname}", state)
      case OCDB_READ_NEXT() =>
        ocdbExec(
          conn.id,
          s"FETCH FORWARD ${GlobalState.getFetchRecords} FROM ${cname}",
          state
        )
    }
  }

  def ocdbCursorClose(id: Int, cname: String, state: OCDBState): Unit =
    lookUpConnList(id, state) match {
      case None => ()
      case Some((_, pConn)) => {
        val res = ocdbPGCursorClose(pConn.connAddr, cname, state)
        updateConnList(id, pConn.setResult(res), state)
      }
    }

  def ocdbPGCursorClose(
      conn: Option[Connection],
      cname: String,
      state: OCDBState
  ): ExecResult =
    ocdbPGExec(conn, s"CLOSE ${cname}")
}
