import jp.osscons.opensourcecobol.libcobj.call.CobolRunnable
import jp.osscons.opensourcecobol.libcobj.data.CobolDataStorage
import scala.collection.immutable._
import java.nio.ByteBuffer
import java.sql.ResultSet
import Operation._
import Common._
import ConstValues._
import SQLVar._
import Select._
import Cursor._
import Prepare._
import GlobalState._

object CobolRunnableWrapper {
  var firstRun = true
}

trait CobolRunnableWrapper extends CobolRunnable {
  override def cancel(): Unit = {}
  override def isActive(): Boolean = true
  override def run(args: CobolDataStorage*): Int = {

    // Set PIC_N Charset
    if (CobolRunnableWrapper.firstRun) {
      GlobalState.setFetchRecords({
        val envValue = System.getenv(GlobalState.FETCH_RECORDS_ENV_VAR_NAME)
        val fetchRecords = Option(envValue) match {
          case Some(x) =>
            x.toIntOption match {
              case Some(fetchSize) if (fetchSize > 0) => fetchSize
              case _                                  => 1
            }
          case _ => 1
        }
        fetchRecords
      })
      CobolRunnableWrapper.firstRun = false
    }

    val state: OCDBState = if (parseSqlCA) {
      OCDBState.initialState(args(0))
    } else {
      new OCDBState(SqlCA.defaultValue, Common.internalState)
    }
    val result = execute(args, state)
    if (parseSqlCA) {
      OCDBState.updateByState(args(0), state)
    } else {
      OCDBState.updateByState(state)
    }
    Common.internalState = state.globalState
    result
  }

  def execute(args: Seq[CobolDataStorage], state: OCDBState): Int

  var parseSqlCA: Boolean = true

  def storageToInt(storage: CobolDataStorage): Option[Int] = {
    try {
      for {
        s <- Option(storage)
        n <- Some(ByteBuffer.wrap(s.getByteArray(0, 4)).getInt())
      } yield n
    } catch {
      case e: Throwable => None
    }
  }

  def storageToString(
      stringStorage: CobolDataStorage,
      lengthStorage: CobolDataStorage
  ): Option[String] = {
    try {
      for {
        length <- storageToInt(lengthStorage)
        s <- Option(stringStorage)
        str <- Some(new String(s.getByteArray(0, length)))
      } yield str
    } catch {
      case e: Throwable => None
    }
  }
}

class OCESQLConnect extends CobolRunnableWrapper {
  override def execute(args: Seq[CobolDataStorage], state: OCDBState): Int = {
    val user: Option[String] = storageToString(args(1), args(2))
    val passwd: Option[String] = storageToString(args(3), args(4))
    val name: Option[String] = storageToString(args(5), args(6))
    logLn("OCESQLConnect start")
    OCESQLConnectCore.connect(user, passwd, name, None, state)
  }
}

class OCESQLIDConnect extends CobolRunnableWrapper {
  override def execute(args: Seq[CobolDataStorage], state: OCDBState): Int = {
    val _atdb: Option[String] = storageToString(args(1), args(2))
    val user: Option[String] = storageToString(args(3), args(4))
    val passwd: Option[String] = storageToString(args(5), args(6))
    val name: Option[String] = storageToString(args(7), args(8))
    val atdb = _atdb.map(OCESQLConnectCore.getStrWithoutAfterSpace(_))

    def errorProc() = {
      setLibErrorStatus(OCDB_VAR_NOT_CHAR(), state)
      1
    }

    logLn("OCESQLIDConnect start")
    atdb match {
      case None => errorProc
      case Some(a) =>
        if (a.isEmpty) {
          errorProc
        } else {
          OCESQLConnectCore.connect(user, passwd, name, atdb, state)
        }
    }
  }
}

class OCESQConnectShort extends CobolRunnableWrapper {
  override def execute(args: Seq[CobolDataStorage], state: OCDBState): Int = {
    logLn("OCESQLConnectShort start")
    OCESQLConnectCore.connect(None, None, None, None, state)
  }
}

class OCESQIDConnectShort extends CobolRunnableWrapper {
  override def execute(args: Seq[CobolDataStorage], state: OCDBState): Int = {
    val _atdb: Option[String] = storageToString(args(1), args(2))
    val atdb = _atdb.map(OCESQLConnectCore.getStrWithoutAfterSpace(_))
    logLn("OCESQLIDConnectShort start")
    OCESQLConnectCore.connect(None, None, None, atdb, state)
  }
}

class OCESQLConnectInformal extends CobolRunnableWrapper {
  override def execute(args: Seq[CobolDataStorage], state: OCDBState): Int = {
    val connInfo = storageToString(args(1), args(2))
    logLn("OCESQLConnectInformal start")
    OCESQLConnectCore.connectInformal(connInfo, None, state)
  }
}

class OCESQLIDConnectInformal extends CobolRunnableWrapper {
  override def execute(args: Seq[CobolDataStorage], state: OCDBState): Int = {
    val atdb = storageToString(args(1), args(2))
    val connInfo = storageToString(args(3), args(4))
    logLn("OCESQLIDConnectInformal start")
    OCESQLConnectCore.connectInformal(connInfo, atdb, state)
  }
}

class OCESQLPrepare extends CobolRunnableWrapper {
  override def execute(args: Seq[CobolDataStorage], state: OCDBState): Int = {
    val sname = getCString(args(1)).getOrElse("")
    val query = parsePrepareQuery(args(2), args(3))

    val nParams = "\\?".r.findAllIn(query).length

    logLn(s"Add prepare sname:${sname}, nParams:${nParams}, query:'${query}'")
    addQueryInfoMap(sname, query, nParams, state)
    0
  }
}

class OCESQLDisconnect extends CobolRunnableWrapper {
  override def execute(args: Seq[CobolDataStorage], state: OCDBState): Int =
    resolveCONNID(OCESQL_DEFAULT_DB_NAME, state) match {
      case None => {
        errorLogLn("connection id is not found.")
        setLibErrorStatus(OCDB_NO_CONN(), state)
        1
      }
      case Some(c) => {
        OCESQLDisconnectCore.disconnect(c.id, state)
        0
      }
    }
}

class OCESQLIDDisconnect extends CobolRunnableWrapper {
  override def execute(args: Seq[CobolDataStorage], state: OCDBState): Int = {
    val atdb = storageToString(args(1), args(2))
    resolveCONNID(atdb.getOrElse(""), state) match {
      case None => {
        errorLogLn("connection id is not found.")
        setLibErrorStatus(OCDB_NO_CONN(), state)
        1
      }
      case Some(c) => {
        OCESQLDisconnectCore.disconnect(c.id, state)
        0
      }
    }
  }
}

class OCESQLExec extends CobolRunnableWrapper {
  override def execute(args: Seq[CobolDataStorage], state: OCDBState): Int = {
    val query = getCString(args(1))
    logLn("OCESQLExec start")
    logLn(s"SQL:#${query}#")
    resolveCONNID(OCESQL_DEFAULT_DB_NAME, state) match {
      case None => {
        errorLogLn("connection id is not found")
        setLibErrorStatus(OCDB_NO_CONN(), state)
        1
      }
      case Some(connInfo) => {
        ocesqlExec(connInfo.id, query, state)
        0
      }
    }
  }
}

class OCESQLExecWhereCurrentOf extends CobolRunnableWrapper {
  override def execute(args: Seq[CobolDataStorage], state: OCDBState): Int = {
    val query = getCString(args(1))
    val cursorName = getCString(args(2))
    logLn("OCESQLExecWhereCurrentOf start")
    logLn(s"SQL:#${query}#")
    resolveCONNID(OCESQL_DEFAULT_DB_NAME, state) match {
      case None => {
        errorLogLn("connection id is not found")
        setLibErrorStatus(OCDB_NO_CONN(), state)
        1
      }
      case Some(connInfo) => {
        ocesqlExecWhereCurrentOf(connInfo.id, query, cursorName, state)
        0
      }
    }
  }
}

class OCESQLIDExec extends CobolRunnableWrapper {
  override def execute(args: Seq[CobolDataStorage], state: OCDBState): Int = {
    val atdb = storageToString(args(1), args(2))
    val query = getCString(args(3))
    logLn("OCESQLIDExec start")
    logLn(s"SQL:#${query}#")
    resolveCONNID(atdb.getOrElse(""), state) match {
      case None => {
        errorLogLn("connection id is not found")
        setLibErrorStatus(OCDB_NO_CONN(), state)
        1
      }
      case Some(connInfo) => {
        ocesqlExec(connInfo.id, query, state)
        0
      }
    }
  }
}

class OCESQLExecParams extends CobolRunnableWrapper {
  override def execute(args: Seq[CobolDataStorage], state: OCDBState): Int = {
    val query = getCString(args(1))
    val nParams = storageToInt(args(2))
    logLn("OCESQLExecParams start")
    logLn(s"SQL:#${query.getOrElse("")}#")
    resolveCONNID(OCESQL_DEFAULT_DB_NAME, state) match {
      case None => {
        errorLogLn("connection id is not found")
        setLibErrorStatus(OCDB_NO_CONN(), state)
        1
      }
      case Some(connectionInfo) => {
        ocesqlExecParams(connectionInfo.id, query, nParams.getOrElse(0), state)
        0
      }
    }
  }
}

class OCESQLExecParamsWhereCurrentOf extends CobolRunnableWrapper {
  override def execute(args: Seq[CobolDataStorage], state: OCDBState): Int = {
    val query = getCString(args(1))
    val nParams = storageToInt(args(2))
    val cursorName = getCString(args(3))
    logLn("OCESQLExecParamsWhereCurrentOf start")
    logLn(s"SQL:#${query}#")
    resolveCONNID(OCESQL_DEFAULT_DB_NAME, state) match {
      case None => {
        errorLogLn("connection id is not found")
        setLibErrorStatus(OCDB_NO_CONN(), state)
        1
      }
      case Some(connInfo) => {
        ocesqlExecParamsWhereCurrentOf(
          connInfo.id,
          query,
          nParams.getOrElse(0),
          cursorName,
          state
        )
        0
      }
    }
  }
}

class OCESQLIDExecParams extends CobolRunnableWrapper {
  override def execute(args: Seq[CobolDataStorage], state: OCDBState): Int = {
    val atdb = storageToString(args(1), args(2))
    val query = getCString(args(3))
    val nParams = storageToInt(args(4))
    logLn("OCESQLIDExecParams start")
    resolveCONNID(atdb.getOrElse(""), state) match {
      case None => {
        errorLogLn("connection id is not found")
        setLibErrorStatus(OCDB_NO_CONN(), state)
        1
      }
      case Some(connectionInfo) => {
        ocesqlExecParams(connectionInfo.id, query, nParams.getOrElse(0), state)
        0
      }
    }
  }
}

class OCESQLExecParamsOccurs extends CobolRunnableWrapper {
  override def execute(args: Seq[CobolDataStorage], state: OCDBState): Int = {
    val query = getCString(args(1))
    val nParams = storageToInt(args(2))
    logLn("OCESQLExecParamsOccurs start")
    logLn(s"SQL:#${query.getOrElse("")}#")
    resolveCONNID(OCESQL_DEFAULT_DB_NAME, state) match {
      case None => {
        errorLogLn("connection id is not found")
        setLibErrorStatus(OCDB_NO_CONN(), state)
        1
      }
      case Some(c) => {
        ocesqlExecParamsOccurs(c.id, query, nParams.getOrElse(0), state)
        0
      }
    }
  }
}

class OCESQLIDExecParamsOccurs extends CobolRunnableWrapper {
  override def execute(args: Seq[CobolDataStorage], state: OCDBState): Int = {
    val atdb = storageToString(args(1), args(2))
    val query = getCString(args(3))
    val nParams = storageToInt(args(4))
    logLn("OCESQLIDExecParamsOccurs start")
    resolveCONNID(atdb.getOrElse(""), state) match {
      case None => {
        errorLogLn("connection id is not found")
        setLibErrorStatus(OCDB_NO_CONN(), state)
        1
      }
      case Some(connectionInfo) => {
        ocesqlExecParamsOccurs(
          connectionInfo.id,
          query,
          nParams.getOrElse(0),
          state
        )
        0
      }
    }
  }
}

class OCESQLCursorDeclare extends CobolRunnableWrapper {
  override def execute(args: Seq[CobolDataStorage], state: OCDBState): Int = {
    val cname = getCString(args(1))
    val query = getCString(args(2))
    logLn("OCESQLCursorDeclare start")
    logLn(s"SQL:#${query}#")
    resolveCONNID(OCESQL_DEFAULT_DB_NAME, state) match {
      case None => {
        errorLogLn("connection id is not found")
        setLibErrorStatus(OCDB_NO_CONN(), state)
        return 1
      }
      case Some(c) => {
        ocesqlCursorDeclare(c.id, cname, query, 0, state)
        return 0
      }
    }
  }
}

class OCESQLIDCursorDeclare extends CobolRunnableWrapper {
  override def execute(args: Seq[CobolDataStorage], state: OCDBState): Int = {
    val atdb = storageToString(args(1), args(2))
    val cname = getCString(args(3))
    val query = getCString(args(4))
    logLn("OCESQLIDCursorDeclare start")
    resolveCONNID(atdb.getOrElse(""), state) match {
      case None => {
        errorLogLn("connection id is not found")
        setLibErrorStatus(OCDB_NO_CONN(), state)
        1
      }
      case Some(c) => {
        ocesqlCursorDeclare(c.id, cname, query, 0, state)
        0
      }
    }
  }
}

class OCESQLCursorDeclareParams extends CobolRunnableWrapper {
  override def execute(args: Seq[CobolDataStorage], state: OCDBState): Int = {
    val cname = getCString(args(1))
    val query = getCString(args(2))
    val nParams = storageToInt(args(3))

    logLn("OCESQLCursorDeclareParams start")
    logLn(s"SQL:#${query}#")
    resolveCONNID(OCESQL_DEFAULT_DB_NAME, state) match {
      case None => {
        errorLogLn("connection id is not found")
        setLibErrorStatus(OCDB_NO_CONN(), state)
        return 1
      }
      case Some(c) => {
        ocesqlCursorDeclare(c.id, cname, query, nParams.getOrElse(0), state)
        return 0
      }
    }
  }
}

class OCESQLIDCursorDeclareParams extends CobolRunnableWrapper {
  override def execute(args: Seq[CobolDataStorage], state: OCDBState): Int = {
    val atdb = storageToString(args(1), args(2))
    val cname = getCString(args(3))
    val query = getCString(args(4))
    val nParams = storageToInt(args(5))
    logLn("OCESQLIDCursorDeclareParams start")
    resolveCONNID(atdb.getOrElse(""), state) match {
      case None => {
        errorLogLn("connection id is not found")
        setLibErrorStatus(OCDB_NO_CONN(), state)
        1
      }
      case Some(c) => {
        ocesqlCursorDeclare(c.id, cname, query, nParams.getOrElse(0), state)
        0
      }
    }
  }
}

class OCESQLPreparedCursorDeclare extends CobolRunnableWrapper {
  override def execute(args: Seq[CobolDataStorage], state: OCDBState): Int = {
    val cname = getCString(args(1))
    val sname = getCString(args(2))
    logLn("OCESQLPreparedCursorDeclare start")
    resolveCONNID(OCESQL_DEFAULT_DB_NAME, state) match {
      case None => {
        errorLogLn("connection id is not found")
        setLibErrorStatus(OCDB_NO_CONN(), state)
        1
      }
      case Some(c) => {
        ocesqlPreparedCursorDeclare(c.id, cname, sname, state)
        0
      }
    }
  }
}

class OCESQLIDPreparedCursorDeclare extends CobolRunnableWrapper {
  override def execute(args: Seq[CobolDataStorage], state: OCDBState): Int = {
    val atdb = storageToString(args(1), args(2))
    val cname = getCString(args(3))
    val sname = getCString(args(4))
    logLn("OCESQLIDPreparedCursorDeclare start")
    resolveCONNID(atdb.getOrElse(""), state) match {
      case None => {
        errorLogLn("connection id is not found")
        setLibErrorStatus(OCDB_NO_CONN(), state)
        1
      }
      case Some(c) => {
        ocesqlPreparedCursorDeclare(c.id, cname, sname, state)
        0
      }
    }
  }
}

class OCESQLExecPrepare extends CobolRunnableWrapper {
  override def execute(args: Seq[CobolDataStorage], state: OCDBState): Int = {
    val sname = getCString(args(1))
    val nParams = storageToInt(args(2))
    logLn("OCESQLExecPrepare start")
    resolveCONNID(OCESQL_DEFAULT_DB_NAME, state) match {
      case None => {
        errorLogLn("connection id is not found")
        setLibErrorStatus(OCDB_NO_CONN(), state)
        1
      }
      case Some(c) => {
        ocesqlExecPrepare(c.id, sname, nParams.getOrElse(0), state)
        0
      }
    }
  }
}

class OCESQLIDExecPrepare extends CobolRunnableWrapper {
  override def execute(args: Seq[CobolDataStorage], state: OCDBState): Int = {
    val atdb = storageToString(args(1), args(2))
    val sname = getCString(args(3))
    val nParams = storageToInt(args(4))

    logLn("OCESQLIDExecPrepare start")

    resolveCONNID(atdb.getOrElse(""), state) match {
      case None => {
        errorLogLn("connection id is not found")
        setLibErrorStatus(OCDB_NO_CONN(), state)
        1
      }
      case Some(c) => {
        ocesqlExecPrepare(c.id, sname, nParams.getOrElse(0), state)
        0
      }
    }
  }
}

class OCESQLCursorOpen extends CobolRunnableWrapper {
  override def execute(args: Seq[CobolDataStorage], state: OCDBState): Int = {
    val cname = getCString(args(1))
    val name = cname.getOrElse("")

    logLn("OCESQLIDExecPrepare start")
    state.updateSQLCA(SqlCA.defaultValue)
    logLn(s"cname=#${cname.getOrElse("")}#")

    var cursor = getCursor(name, cname, state) match {
      case None    => return 1
      case Some(c) => c
    }

    updateCursorMap(name, cursor, state)
    dispatch(cursor, state)

    if (!setResultStatus(cursor.connId, state)) {
      return 1
    }

    ocdbCursorOpen(cursor.connId, name, state)
    if (!setResultStatus(cursor.connId, state)) {
      return 1
    }
    updateCursorMap(name, cursor.setIsOpened(true), state)
    return 0
  }

  private def getCursor(
      name: String,
      cname: Option[String],
      state: OCDBState
  ): Option[Cursor] = {
    if (name == "") {
      setLibErrorStatus(OCDB_EMPTY(), state)
      return None
    }
    val optionCursor = getCursorFromMap(name, state)

    if (optionCursor.isEmpty) {
      errorLogLn(s"cursor ${name} not registered.")
      setLibErrorStatus(OCDB_WARNING_UNKNOWN_PORTAL(), state)
      return None
    }

    var cursor_ = optionCursor.getOrElse(Cursor.defaultValue)

    if (cursor_.isOpened) {
      logLn(s"cursor ${cname} already opened")
      if (!setResultStatus(cursor_.connId, state)) {
        errorLogLn(s"cursor ${name} close failed")
        return None
      }
    }

    Some(cursor_.setIsOpened(false))
  }

  private def dispatch(cursor: Cursor, state: OCDBState): Unit = {
    if (cursor.nParams > 0) {
      // TODO the following code should be improved
      val args = cursor.sqlVarQueue.map(sv => createRealData(sv, 0, state))
      ocdbCursorDeclareParams(
        cursor.connId,
        cursor.name,
        cursor.query,
        args,
        OCDB_CURSOR_WITH_HOLD_OFF,
        state
      )
    } else {
      cursor.sp match {
        case q :: _ =>
          ocdbCursorDeclare(
            cursor.connId,
            cursor.name,
            q.query,
            OCDB_CURSOR_WITH_HOLD_OFF,
            state
          )
        case _ =>
          ocdbCursorDeclare(
            cursor.connId,
            cursor.name,
            cursor.query,
            OCDB_CURSOR_WITH_HOLD_OFF,
            state
          )
      }
    }
  }
}

object OCESQLCursorOpenParams {
  val ERROR_MESSAGE_NUMBER_OF_PARAMETER =
    "A number of parameters and prepared sql parameters is unmatch.".getBytes()
}

class OCESQLCursorOpenParams extends CobolRunnableWrapper {
  override def execute(args: Seq[CobolDataStorage], state: OCDBState): Int = {
    val cname = getCString(args(1))
    val name = cname.getOrElse("")
    val nParams = storageToInt(args(2)).getOrElse(0)

    logLn("OCESQLIDExecPrepare start")
    state.initSqlca()
    logLn(s"cname=#${cname.getOrElse("")}#")

    val cursor = getCursor(name, state) match {
      case None    => return 1
      case Some(c) => c
    }

    cursor.sp match {
      case Nil => {
        errorLogLn(s"prepare sql in cursor ${name} not registred.")
        setLibErrorStatus(OCDB_INVALID_STMT(), state)
        return 1
      }
      case sp :: _ if (sp.nParams != nParams) => {
        errorLogLn(
          s"A number of parameters(${nParams}) and prepared sql parameters(${sp.nParams}) is unmatch"
        )
        setLibErrorStatus(OCDB_EMPTY(), state)
        val errorMessage =
          OCESQLCursorOpenParams.ERROR_MESSAGE_NUMBER_OF_PARAMETER
        val newSqlCA = state.sqlCA
          .setErrmc(errorMessage)
          .setErrml(errorMessage.length.toShort)
        state.updateSQLCA(newSqlCA)
        return 1
      }
      case _ => ()
    }

    if (cursor.isOpened) {
      logLn(s"cursor ${cname} already opened")
      if (!setResultStatus(cursor.connId, state)) {
        errorLogLn(s"cursor ${name} close failed")
        return 1
      }
    }
    // TODO implement precisely

    updateState(cursor.setIsOpened(false), name, state)
  }

  private def getCursor(name: String, state: OCDBState): Option[Cursor] = {
    if (name == "") {
      setLibErrorStatus(OCDB_EMPTY(), state)
      return None
    }

    getCursorFromMap(name, state) match {
      case None => {
        errorLogLn(s"cursor ${name} not registered.")
        setLibErrorStatus(OCDB_WARNING_UNKNOWN_PORTAL(), state)
        None
      }
      case Some(c) => Some(c)
    }
  }

  private def updateState(
      cursor: Cursor,
      name: String,
      state: OCDBState
  ): Int = {
    updateCursorMap(name, cursor, state)

    if (!setResultStatus(cursor.connId, state)) {
      return 1
    }

    ocdbCursorOpen(cursor.connId, name, state)
    if (!setResultStatus(cursor.connId, state)) {
      return 1
    }

    updateCursorMap(name, cursor.setIsOpened(true), state)
    0
  }
}

object OCESQLCursorFetchOne {
  private val ERROR_MESSAGE_NUMBER_OF_PARAMETER =
    "A number of Parameters and results is unmatch.".getBytes()
}

class OCESQLCursorFetchOne extends CobolRunnableWrapper {
  def reFetch(
      state: OCDBState,
      name: String,
      cname: Option[String],
      id: Int
  ): Int = {
    ocdbCursorFetchOne(id, name, OCDB_READ_NEXT(), state)
    val resultStatus = setResultStatus(id, state)
    if (!resultStatus) {
      return 1
    }

    val fields = ocdbNfields(id, state)

    if (fields != state.globalState.sqlResVarQueue.length) {
      errorLogLn(
        s"A number of parameters ${state.globalState.sqlResVarQueue.length} " +
          s"and results(${fields}) is unmatch."
      )
      setLibErrorStatus(OCDB_EMPTY(), state)
      val sqlCA = state.sqlCA
      val errorMessage = OCESQLCursorFetchOne.ERROR_MESSAGE_NUMBER_OF_PARAMETER
      state.updateSQLCA(
        sqlCA.setErrmc(errorMessage).setErrml(errorMessage.length.toShort)
      )
      return 1
    }

    lookUpConnList(id, state) match {
      case None => setLibErrorStatus(OCDB_NOT_FOUND(), state)
      case Some((_, conn)) =>
        conn.result match {
          case Right(EResultSet(rs)) => {
            getCursorFromMap(name, state) match {
              case None => {
                errorLogLn(s"cursor ${name} not registered.")
                setLibErrorStatus(OCDB_WARNING_UNKNOWN_PORTAL(), state)
              }
              case Some(cursor) => saveRecords(rs, fields, name, state)
            }
          }
          case _ =>
            setLibErrorStatus(OCDB_NOT_FOUND(), state)
        }
    }
    return 0
  }

  private def saveRecords(
      rs: ResultSet,
      fields: Int,
      name: String,
      state: OCDBState
  ): Unit = {
    var fetchRecords = List.empty[List[Option[Array[Byte]]]]
    while (rs.next()) {
      var fetchRecord = List.empty[Option[Array[Byte]]]
      for ((sv, i) <- state.globalState.sqlResVarQueue.zipWithIndex) {
        if (i < fields) {
          fetchRecord = fetchRecord ::: List(ocdbGetValue(rs, sv, i + 1))
        }
      }
      fetchRecords = fetchRecords ::: List(fetchRecord)
    }
    updateFetchRecords(name, fetchRecords, true, state)
  }

  def popRecord(
      state: OCDBState,
      record: List[Option[Array[Byte]]],
      restRecords: List[List[Option[Array[Byte]]]],
      id: Int,
      name: String
  ): Int = {
    for ((sv, i) <- state.globalState.sqlResVarQueue.zipWithIndex) {
      if (i < record.length) {
        record(i) match {
          case Some(str) =>
            createCobolData(sv, 0, str, state.globalState.occursInfo)
          case None => ()
        }
      }
    }
    updateFetchRecords(name, restRecords, false, state)
    0
  }

  override def execute(args: Seq[CobolDataStorage], state: OCDBState): Int = {
    val cname = getCString(args(1))
    val name = cname.getOrElse("")

    state.initSqlca()
    if (name.length == 0) {
      setLibErrorStatus(OCDB_EMPTY(), state)
      return 1
    }

    logLn(s"cname:${name}")
    getCursorFromMap(name, state) match {
      case None => {
        errorLogLn(s"cursor ${name} not registered.")
        setLibErrorStatus(OCDB_WARNING_UNKNOWN_PORTAL(), state)
        1
      }
      case Some(cursor) => {
        val id = cursor.connId
        val retCode = getRecord(cursor, name, cname, id, state)
        setFinalState(name, cname, cursor, state)
        retCode
      }
    }
  }

  private def setFinalState(
      name: String,
      cname: Option[String],
      cursor: Cursor,
      state: OCDBState
  ): Unit = {
    val newTuples = cursor.tuples + state.sqlCA.errd(2)
    for {
      cursor_name <- cname
      c <- state.globalState.cursorMap.get(cursor_name)
    } yield updateCursorMap(name, c.setTuples(newTuples), state)
    var sqlCA = state.sqlCA
    sqlCA.errd(2) = newTuples
    state.updateSQLCA(sqlCA)
  }

  private def getRecord(
      cursor: Cursor,
      name: String,
      cname: Option[String],
      id: Int,
      state: OCDBState
  ): Int =
    cursor.fetchRecords match {
      case Nil => {
        reFetch(state, name, cname, id)
        getCursorFromMap(name, state) match {
          case None => {
            setLibErrorStatus(OCDB_NOT_FOUND(), state)
            1
          }
          case Some(cursor) =>
            cursor.fetchRecords match {
              case Nil => {
                if (state.sqlCA.code != -9999) {
                  setLibErrorStatus(OCDB_NOT_FOUND(), state)
                }
                1
              }
              case record :: restRecords => {
                popRecord(state, record, restRecords, id, name)
              }
            }
        }
      }
      case record :: restRecords =>
        popRecord(state, record, restRecords, id, name)
    }
}

object OCESQLCursorFetchOccurs {
  private val ERROR_MESSAGE_NUMBER_OF_PARAMETER =
    s"A number of Parameters and results is unmatch.".getBytes()
}

class OCESQLCursorFetchOccurs extends CobolRunnableWrapper {
  override def execute(args: Seq[CobolDataStorage], state: OCDBState): Int = {
    val cname = getCString(args(1))
    val name = cname.getOrElse("")
    state.initSqlca()
    if (name.length == 0) {
      setLibErrorStatus(OCDB_EMPTY(), state)
      return 1
    }

    logLn(s"cname:${name}")
    getCursorFromMap(name, state) match {
      case None => {
        errorLogLn(s"cursor ${name} not registered.")
        setLibErrorStatus(OCDB_WARNING_UNKNOWN_PORTAL(), state)
        1
      }
      case Some(cursor) => {
        val id = cursor.connId
        ocdbCursorFetchOccurs(
          id,
          name,
          OCDB_READ_NEXT(),
          state.globalState.occursInfo.iter,
          state
        )
        if (state.sqlCA.code < 0) {
          return 1
        }
        val fields = ocdbNfields(id, state)

        if (fields != state.globalState.sqlResVarQueue.length) {
          return lengthUnmatchError(fields, state)
        }

        val optionTuple = getTuple(id, name, cursor, state)
        setFinalState(optionTuple, state)
      }
    }
  }

  private def lengthUnmatchError(fields: Int, state: OCDBState): Int = {
    errorLogLn(
      s"A number of parameters ${state.globalState.sqlResVarQueue.length} " +
        s"and results(${fields} is unmatch."
    )
    setLibErrorStatus(OCDB_EMPTY(), state)
    val sqlCA = state.sqlCA
    state.updateSQLCA(
      sqlCA
        .setErrmc(
          OCESQLCursorFetchOccurs.ERROR_MESSAGE_NUMBER_OF_PARAMETER
        )
        .setErrml(
          OCESQLCursorFetchOccurs.ERROR_MESSAGE_NUMBER_OF_PARAMETER.length.toShort
        )
    )
    1
  }

  private def getTuple(
      id: Int,
      name: String,
      cursor: Cursor,
      state: OCDBState
  ): Option[Int] =
    lookUpConnList(id, state) match {
      case Some((_, conn)) =>
        conn.result match {
          case Right(EResultSet(rs)) => {
            val tuples = resultSetToSqlVar(
              rs,
              0,
              0,
              state.globalState.sqlResVarQueue,
              state.globalState.occursInfo
            )
            updateCursorMap(name, cursor.setTuples(tuples), state)
            Option(tuples)
          }
          case _ => {
            setLibErrorStatus(OCDB_NOT_FOUND(), state)
            None
          }
        }
      case _ => {
        setLibErrorStatus(OCDB_NOT_FOUND(), state)
        None
      }
    }

  private def setFinalState(optionTuple: Option[Int], state: OCDBState): Int = {
    var sqlCA = state.sqlCA
    sqlCA.errd(2) = optionTuple.getOrElse(0)
    val newSqlCA = optionTuple match {
      case Some(tuples) => sqlCA.setCode(0)
      case _            => sqlCA
    }
    state.updateSQLCA(newSqlCA)
    0
  }
}

class OCESQLCursorClose extends CobolRunnableWrapper {
  override def execute(args: Seq[CobolDataStorage], state: OCDBState): Int = {
    val cname = getCString(args(1))
    val name = cname.getOrElse("")
    state.initSqlca()
    if (name == "") {
      setLibErrorStatus(OCDB_EMPTY(), state)
      return 1
    } else {
      logLn(s"Cursor Name: ${name}")
      getCursorFromMap(name, state) match {
        case None => {
          errorLogLn(s"cursor ${name} not registered")
          setLibErrorStatus(OCDB_WARNING_UNKNOWN_PORTAL(), state)
          return 1
        }
        case Some(cursor) if !cursor.isOpened => {
          logLn(s"cursor ${name} not opened.")
          return 0
        }
        case Some(cursor) => {
          logLn(s"Connect ID: ${cursor.connId}")
          ocdbCursorClose(cursor.connId, name, state)
          if (setResultStatus(cursor.connId, state)) {
            updateCursorMap(name, cursor.setIsOpened(false), state)
            return 0
          } else {
            return 1
          }
        }
      }
    }
  }
}

class OCESQLExecSelectIntoOne extends CobolRunnableWrapper {
  override def execute(args: Seq[CobolDataStorage], state: OCDBState): Int = {
    val query = getCString(args(1))
    val nParams = storageToInt(args(2))
    val nResParams = storageToInt(args(3))
    logLn("OCESQLExecSelectIntoOne start")
    logLn(s"SQL:#${query}#")
    resolveCONNID(OCESQL_DEFAULT_DB_NAME, state) match {
      case None => {
        errorLogLn("connection id is not found")
        setLibErrorStatus(OCDB_NO_CONN(), state)
        1
      }
      case Some(connectionInfo) => {
        ocesqlExecSelectIntoOne(
          connectionInfo.id,
          query,
          nParams.getOrElse(0),
          nResParams.getOrElse(0),
          state
        )
        0
      }
    }
  }
}

class OCESQLIDExecSelectIntoOne extends CobolRunnableWrapper {
  override def execute(args: Seq[CobolDataStorage], state: OCDBState): Int = {
    val atdb = storageToString(args(1), args(2))
    val query = getCString(args(3))
    val nParams = storageToInt(args(4))
    val nResParams = storageToInt(args(5))
    logLn("OCESQLIDExecSelectIntoOne start")
    resolveCONNID(atdb.getOrElse(""), state) match {
      case None => {
        errorLogLn("connection id is not found")
        setLibErrorStatus(OCDB_NO_CONN(), state)
        1
      }
      case Some(connectionInfo) => {
        ocesqlExecSelectIntoOne(
          connectionInfo.id,
          query,
          nParams.getOrElse(0),
          nResParams.getOrElse(0),
          state
        )
        0
      }
    }
  }
}

class OCESQLExecSelectIntoOccurs extends CobolRunnableWrapper {
  override def execute(args: Seq[CobolDataStorage], state: OCDBState): Int = {
    val query = getCString(args(1))
    val nParams = storageToInt(args(2))
    val nResParams = storageToInt(args(3))
    logLn("OCESQLExecSelectIntoOccurs start")
    resolveCONNID(OCESQL_DEFAULT_DB_NAME, state) match {
      case None => {
        errorLogLn("connection id is not found")
        setLibErrorStatus(OCDB_NO_CONN(), state)
        1
      }
      case Some(connectionInfo) => {
        ocesqlExecSelectIntoOccurs(
          connectionInfo.id,
          query,
          nParams.getOrElse(0),
          nResParams.getOrElse(0),
          state
        )
        0
      }
    }
  }
}

class OCESQLIDExecSelectIntoOccurs extends CobolRunnableWrapper {
  override def execute(args: Seq[CobolDataStorage], state: OCDBState): Int = {
    val atdb = storageToString(args(1), args(2))
    val query = getCString(args(3))
    val nParams = storageToInt(args(4))
    val nResParams = storageToInt(args(5))
    logLn("OCESQLIDExecSelectIntoOccurs start")
    resolveCONNID(atdb.getOrElse(""), state) match {
      case None => {
        errorLogLn("connection id is not found")
        setLibErrorStatus(OCDB_NO_CONN(), state)
        1
      }
      case Some(connectionInfo) => {
        ocesqlExecSelectIntoOccurs(
          connectionInfo.id,
          query,
          nParams.getOrElse(0),
          nResParams.getOrElse(0),
          state
        )
        0
      }
    }
  }
}

class OCESQLStartSQL extends CobolRunnableWrapper {
  parseSqlCA = false
  override def execute(args: Seq[CobolDataStorage], state: OCDBState): Int = {
    logLn("#begin")
    resetSqlVarQueue(state)
    logLn("#end")
    0
  }
}

class OCESQLSetSQLParams extends CobolRunnableWrapper {
  parseSqlCA = false
  override def execute(args: Seq[CobolDataStorage], state: OCDBState): Int = {
    val paramType = storageToInt(args(0)).getOrElse(0)
    val paramLength = storageToInt(args(1)).getOrElse(0)
    val scale = storageToInt(args(2)).getOrElse(0)
    val storage = args(3)
    val oStorage = Option(storage)

    if (paramType < OCDB_TYPE_MIN || paramType > OCDB_TYPE_MAX) {
      errorLogLn(s"invalid argument 'type': ${paramType}")
      return 1
    }
    if (paramLength < 0) {
      errorLogLn(s"invalid argument 'length': ${paramLength}")
      return 1
    }
    if (oStorage.isEmpty) {
      errorLogLn(s"finvalid argument addr is NULL")
      return 1
    }
    addSqlVarQueue(paramType, paramLength, scale, oStorage, state)
    0
  }
}

class OCESQLSetResultParams extends CobolRunnableWrapper {
  parseSqlCA = false
  override def execute(args: Seq[CobolDataStorage], state: OCDBState): Int = {
    val paramType = storageToInt(args(0)).getOrElse(0)
    val paramLength = storageToInt(args(1)).getOrElse(0)
    val scale = storageToInt(args(2)).getOrElse(0)
    val storage = args(3)
    val oStorage = Option(storage)

    if (paramType < OCDB_TYPE_MIN || paramType > OCDB_TYPE_MAX) {
      errorLogLn(s"invalid argument 'type': ${paramType}")
      return 1
    }
    if (paramLength < 0) {
      errorLogLn(s"invalid argument 'length': ${paramLength}")
      return 1
    }
    if (oStorage.isEmpty) {
      errorLogLn(s"finvalid argument addr is NULL")
      return 1
    }
    addSqlResVarQueue(paramType, paramLength, scale, oStorage, state)
    0
  }
}

class OCESQLSetHostTable extends CobolRunnableWrapper {
  parseSqlCA = false
  override def execute(args: Seq[CobolDataStorage], state: OCDBState): Int = {
    val iter = storageToInt(args(0)).getOrElse(0)
    val length = storageToInt(args(1)).getOrElse(0)
    val isParent = storageToInt(args(2)).getOrElse(0) != 0

    if (iter < 0) {
      errorLogLn(s"invalid argument 'iter': ${iter}")
      1
    } else if (length < 0) {
      errorLogLn(s"invalid argument 'length': ${length}")
      1
    } else {
      val globalState = state.globalState
      val newGlobalState =
        globalState.setOccursInfo(new OccursInfo(iter, length, isParent))
      state.updateGlobalState(newGlobalState)
      0
    }
  }
}

class OCESQLEndSQL extends CobolRunnableWrapper {
  parseSqlCA = false

  override def execute(args: Seq[CobolDataStorage], state: OCDBState): Int = {
    if(isLoggingEnable()){
      logLn("#debug start cump var_list")
      showSqlVarQueue(state.globalState.sqlVarQueue)
      logLn("#debug start dump res_list")
      showSqlVarQueue(state.globalState.sqlResVarQueue)
    }
    resetSqlVarQueue(state)
    0
  }
}
