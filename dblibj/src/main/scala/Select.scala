import ConstValues._
import Operation._
import Common._

import java.sql.ResultSet
import java.text.SimpleDateFormat
import scala.collection.immutable.Queue
import java.nio.charset.StandardCharsets

object Select {
  def getPreviousOperationResultSet(
      id: Int,
      state: OCDBState
  ): Option[ResultSet] =
    lookUpConnList(id, state) match {
      case None => None
      case Some((_, pConn)) =>
        pConn.result match {
          case Right(EResultSet(rs)) => Some(rs)
          case _                     => None
        }
    }

  private def errorProc(state: OCDBState): Unit = {
    setLibErrorStatus(OCDB_NOT_FOUND(), state)
    logLn("TUPLES NODATA")
  }

  def ocesqlExecSelectIntoOne(
      id: Int,
      query: Option[String],
      nParams: Int,
      nResParams: Int,
      state: OCDBState
  ): Unit = {
    if (query.getOrElse("").length == 0) {
      errorLogLn("ARGUMENT ERROR")
      setLibErrorStatus(OCDB_EMPTY(), state)
      return ()
    }

    if (nParams > 0) {
      ocesqlExecParams(id, query, nParams, state)
    } else {
      ocesqlExec(id, query, state)
    }

    var rs = getPreviousOperationResultSet(id, state)

    val status = setResultStatus(id, state)
    if (!status) {
      return ()
    }

    val fields = ocdbNfields(id, state)
    if (fields != nResParams) {
      errorLogLn(
        s"ResParams(${nResParams}) and fields(${fields}) are different"
      )
      setLibErrorStatus(OCDB_EMPTY(), state)
      return ()
    }
    saveResultSetInSqlResVarQueue(rs, fields, state)
  }

  private def saveResultSetInSqlResVarQueue(
      rs: Option[ResultSet],
      fields: Int,
      state: OCDBState
  ): Unit =
    rs match {
      case None => errorProc(state)
      case Some(rs) =>
        if (rs.next()) {
          for ((sv, i) <- state.globalState.sqlResVarQueue.zipWithIndex) {
            if (i >= fields) {
              ()
            } else {
              ocdbGetValue(rs, sv, i + 1) match {
                case Some(str) =>
                  createCobolData(sv, 0, str, state.globalState.occursInfo)
                case _ => ()
              }
            }
          }
        } else {
          errorProc(state)
        }
    }

  def ocesqlExecSelectIntoOccurs(
      id: Int,
      query: Option[String],
      nParams: Int,
      nResParams: Int,
      state: OCDBState
  ): Unit = {
    if (
      query.getOrElse("").length == 0 || state.globalState.occursInfo.iter > 500
    ) {
      setLibErrorStatus(OCDB_EMPTY(), state)
      return ()
    }

    if (nParams > 0) {
      ocesqlExecParams(id, query, nParams, state)
    } else {
      ocesqlExec(id, query, state)
    }

    if (!setResultStatus(id, state)) {
      return ()
    }

    val fields = ocdbNfields(id, state)
    if (fields != nResParams) {
      errorLogLn(
        s"ResParams(${nResParams}) and fields(${fields}) are different"
      )
      setLibErrorStatus(OCDB_EMPTY(), state)
    }

    lookUpConnList(id, state) match {
      case None => ()
      case Some((_, conn)) =>
        conn.result match {
          case Right(EResultSet(rs)) =>
            resultSetToSqlVarSelectOccurs(
              rs,
              state.globalState.sqlResVarQueue,
              state.globalState.occursInfo
            )
          case _ => ()
        }
    }
  }

  def resultSetToSqlVarSelectOccurs(
      rs: ResultSet,
      sqlVarQueue: Queue[SQLVar],
      occursInfo: OccursInfo
  ): Int = {
    var rowCount = 0
    for (index <- 0 until occursInfo.iter) {
      if (rs.next()) {
        for ((sv, j) <- sqlVarQueue.zipWithIndex) {
          ocdbGetValue(rs, sv, j + 1) match {
            case None         => ()
            case Some(retStr) => createCobolData(sv, index, retStr, occursInfo)
          }
        }
        rowCount += 1
      } else {
        for (sv <- sqlVarQueue) {
          createCobolDataLowValue(sv, index, occursInfo)
        }
      }
    }
    rowCount
  }

  def resultSetToSqlVar(
      rs: ResultSet,
      index: Int,
      count: Int,
      sqlVarQueue: Queue[SQLVar],
      occursInfo: OccursInfo
  ): Int = {
    sqlVarQueue(0).addr
      .getOrElse(nullDataStorage)
      .memset(0, occursInfo.length * occursInfo.iter)
    var rowNum: Int = 0
    while (rs.next()) {
      rowNum = rowNum + 1
      for ((sv, j) <- sqlVarQueue.zipWithIndex) {
        var retStrA = ocdbGetValue(rs, sv, j + 1)
        var retStr: Array[Byte] = retStrA.get
        var ret = createCobolData(sv, rowNum - 1, retStr, occursInfo)
      }
    }
    rowNum
  }

  def ocdbPGntuples(conn: ConnectionInfo): Int =
    conn.result match {
      case Right(EResultSet(rs)) => {
        rs.last()
        val ret = rs.getRow
        rs.beforeFirst()
        ret
      }
      case _ => OCDB_INVALID_NUMBER
    }

  def ocdbNfields(id: Int, state: OCDBState): Int =
    lookUpConnList(id, state) match {
      case None             => OCDB_INVALID_NUMBER
      case Some((_, pConn)) => ocdbPGnfields(pConn)
    }

  def ocdbPGnfields(conn: ConnectionInfo): Int = {
    conn.result match {
      case Right(EResultSet(rs)) => rs.getMetaData.getColumnCount
      case Right(_)              => OCDB_INVALID_NUMBER
      case _                     => OCDB_INVALID_NUMBER
    }
  }

  val dateFormatter = new SimpleDateFormat("YYYY-MM-dd HH:mm:ss")

  // scalastyle:off method.length
  // scalastyle:off cyclomatic.complexity
  def ocdbGetValue(rs: ResultSet, sv: SQLVar, i: Int): Option[Array[Byte]] = {
    lazy val stringConverter = {
      for {
        s <- Option(rs.getString(i))
        bytes <- Option(s.getBytes("SHIFT-JIS"))
      } yield bytes
    }

    lazy val decimalConverter = for {
      decimal <- Option(rs.getBigDecimal(i))
      bytes <- Option(decimal.toString.getBytes())
    } yield bytes

    try {
      rs.getMetaData.getColumnType(i) match {
        case java.sql.Types.CHAR         => stringConverter
        case java.sql.Types.LONGVARCHAR  => stringConverter
        case java.sql.Types.LONGNVARCHAR => stringConverter
        case java.sql.Types.VARCHAR      => stringConverter
        case java.sql.Types.NCHAR        => stringConverter
        case java.sql.Types.NVARCHAR     => stringConverter
        case java.sql.Types.DECIMAL      => decimalConverter
        case java.sql.Types.NUMERIC      => decimalConverter
        case java.sql.Types.TIMESTAMP =>
          for {
            timeStamp <- Option(rs.getTimestamp(i))
            bytes <- Option(dateFormatter.format(timeStamp).getBytes())
          } yield bytes
        case java.sql.Types.DATE =>
          for {
            date <- Option(rs.getDate(i))
            bytes <- Option(dateFormatter.format(date).getBytes())
          } yield bytes
        case java.sql.Types.TINYINT =>
          Option(java.lang.Byte.toString(rs.getByte(i)).getBytes())
        case java.sql.Types.SMALLINT =>
          Option(java.lang.Short.toString(rs.getShort(i)).getBytes())
        case java.sql.Types.INTEGER =>
          Option(java.lang.Integer.toString(rs.getInt(i)).getBytes())
        case java.sql.Types.BIGINT =>
          Option(java.lang.Long.toString(rs.getLong(i)).getBytes())
        case java.sql.Types.BOOLEAN =>
          Option(java.lang.Boolean.toString(rs.getBoolean(i)).getBytes())
        case java.sql.Types.FLOAT =>
          Option(java.lang.Double.toString(rs.getDouble(i)).getBytes())
        case java.sql.Types.DOUBLE =>
          Option(java.lang.Double.toString(rs.getDouble(i)).getBytes())
        case java.sql.Types.REAL =>
          Option(java.lang.Double.toString(rs.getDouble(i)).getBytes())
        case java.sql.Types.ROWID =>
          for {
            rowId <- Option(rs.getRowId(i))
            bytes <- Option(rowId.toString().getBytes)
          } yield bytes
        case java.sql.Types.TIME =>
          for {
            time <- Option(rs.getTime(i))
            bytes <- Option(time.toString().getBytes())
          } yield bytes
        case _ => stringConverter
      }
    } catch {
      case e: Throwable => None
    }
  }
  // scalastyle:on method.length
  // scalastyle:on cyclomatic.complexity
}
