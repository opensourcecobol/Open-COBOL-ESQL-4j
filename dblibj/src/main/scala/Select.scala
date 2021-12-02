import ConstValues._
import Operation._
import Common._

import java.sql.ResultSet
import java.text.SimpleDateFormat
import scala.collection.immutable.Queue

object Select {

  def getPreviousOperationResultSet(id: Int): Operation[Option[ResultSet]] =
    lookUpConnList(id).flatMap(_ match {
      case None => operationPure(None)
      case Some((_, pConn)) => operationPure(pConn.result match {
        case Right(EResultSet(rs)) => Some(rs)
        case _ => None
      })
    })

  private def errorProc: Operation[Unit] = for {
    _ <- setLibErrorStatus(OCDB_NOT_FOUND())
    _ <- logLn("TUPLES NODATA")
  } yield ()

  def ocesqlExecSelectIntoOne(id: Int, query: Option[String], nParams: Int, nResParams: Int): Operation[Unit] = (for {
    state <- operationCPure(getState)
    _ <- whenExecuteAndExit(query.getOrElse("").length == 0, for {
      _ <- errorLogLn("ARGUMENT ERROR")
      _ <- setLibErrorStatus(OCDB_EMPTY())
    } yield ())

    _ <- operationCPure(
      if(nParams > 0) {
        ocesqlExecParams(id, query, nParams)
      } else {
        ocesqlExec(id, query)
      })

    rs <- operationCPure(getPreviousOperationResultSet(id))

    status <- operationCPure(setResultStatus(id))
    _ <- whenExecuteAndExit(!status, operationPure(()))

    fields <- operationCPure(OCDBNfields(id))
    _ <- whenExecuteAndExit(fields != nResParams, for {
      _ <- errorLogLn(s"ResParams(${nResParams}) and fields(${fields}) are different")
      _ <- setLibErrorStatus(OCDB_EMPTY())
    } yield ())

    _ <- operationCPure(rs match {
      case None => errorProc
      case Some(rs) => if (rs.next()) {
        forM(state.globalState.sqlResVarQueue.zipWithIndex) { e => {
          val (sv, i) = e
          if (i >= fields) {
            operationPure(())
          } else {
            for {
              retStr <- OCDBGetValue(id, i + 1)
              _ <- operationPure(retStr match {
                case Some(str) => createCobolData(sv, 0, str, state.globalState.occursInfo)
                case _ => ()
              })
            } yield ()
          }
        }}
      } else {
        errorProc
      }
    })
  } yield ()).eval

  def ocesqlExecSelectIntoOccurs(id: Int, query: Option[String], nParams: Int, nResParams: Int): Operation[Unit] = (for {
    state <- operationCPure(getState)
    _ <- whenExecuteAndExit(query.getOrElse("").length == 0 || state.globalState.occursInfo.iter > 500, for {
      _ <- setLibErrorStatus(OCDB_EMPTY())
      } yield ())

    _ <- operationCPure(
      if(nParams > 0) {
        ocesqlExecParams(id, query, nParams)
      } else {
        ocesqlExec(id, query)
      })

    status <- operationCPure(setResultStatus(id))
    _ <- whenExecuteAndExit(!status, operationPure(()))

    fields <- operationCPure(OCDBNfields(id))
    _ <- whenExecuteAndExit(fields != nResParams, for {
      _ <- errorLogLn(s"ResParams(${nResParams}) and fields(${fields}) are different")
      _ <- setLibErrorStatus(OCDB_EMPTY())
    } yield ())

    _ <- operationCPure(lookUpConnList(id).flatMap(_ match {
      case None => operationPure(0)
      case Some((_, conn)) => conn.result match {
        case Right(EResultSet(rs)) => resultSetToSqlVar(rs, id, 0, 0, state.globalState.sqlResVarQueue, state.globalState.occursInfo)
        case _ => operationPure(0)
      }
    }))
  } yield ()).eval

  def resultSetToSqlVar(rs: ResultSet, id: Int, index: Int, count: Int, sqlVarQueue: Queue[SQLVar], occursInfo: OccursInfo): Operation[Int] = {
    if (index >= occursInfo.iter) {
      operationPure(count)

    } else if(rs.next()){
      for {
        _ <- forM(sqlVarQueue.zipWithIndex) {s => {
          val (sv, j) = s
          OCDBGetValue(id, j + 1).flatMap(_ match {
            case None => operationPure (())
            case Some (retStr) =>
              operationPure (createCobolData (sv, index, retStr, occursInfo))
          })}
        }
        retValue <- resultSetToSqlVar(rs, id, index + 1, count + 1, sqlVarQueue, occursInfo)
      } yield retValue
    } else {
      for {
        _ <- forM(sqlVarQueue) {sv => {
            operationPure(createCobolDataLowValue(sv, index, occursInfo))
          }
        }
        ret <- resultSetToSqlVar(rs, id, index + 1, count, sqlVarQueue, occursInfo)
      } yield ret
    }
  }

  def OCDBNtuples(id: Int): Operation[Int] =
    lookUpConnList(id).flatMap(_ match {
      case None => operationPure(OCDB_INVALID_NUMBER)
      case Some((_, pConn)) => operationPure(OCDB_PGntuples(pConn))
    })

  def OCDB_PGntuples(conn: ConnectionInfo): Int =
    conn.result match {
      case Right(EResultSet(rs)) => {
        rs.last()
        val ret = rs.getRow
        rs.beforeFirst()
        ret
      }
      case _ => OCDB_INVALID_NUMBER
    }

  def OCDBNfields(id: Int): Operation[Int] =
    lookUpConnList(id).flatMap(_ match {
      case None => operationPure(OCDB_INVALID_NUMBER)
      case Some((_, pConn)) => operationPure(OCDB_PGnfields(pConn))
    })

  def OCDB_PGnfields(conn: ConnectionInfo): Int = {
    conn.result match {
      case Right(EResultSet(rs)) => rs.getMetaData.getColumnCount
      case Right(_) => OCDB_INVALID_NUMBER
      case _ => OCDB_INVALID_NUMBER
    }
  }

  def OCDBGetValue(id: Int, i: Int): Operation[Option[Array[Byte]]] = {
    for {
      pConn <- lookUpConnList(id)
      ret <- operationPure(pConn match {
        case Some((_, conn)) => conn.result match {
          case Right(EResultSet(rs)) => {

            lazy val stringConverter = for {
              s <- Option(rs.getString(i))
              bytes <- Option(s.getBytes("SHIFT-JIS"))
            } yield bytes

            lazy val decimalConverter = for {
              decimal <- Option(rs.getBigDecimal(i))
              bytes <- Option(decimal.toString.getBytes())
            } yield bytes

            val dateFormatter = new SimpleDateFormat("YYYY-MM-dd HH:mm:ss")
            try {
              rs.getMetaData.getColumnType(i) match {
                case java.sql.Types.CHAR => stringConverter
                case java.sql.Types.LONGVARCHAR => stringConverter
                case java.sql.Types.LONGNVARCHAR => stringConverter
                case java.sql.Types.VARCHAR => stringConverter
                case java.sql.Types.NCHAR => stringConverter
                case java.sql.Types.NVARCHAR => stringConverter
                case java.sql.Types.DECIMAL => decimalConverter
                case java.sql.Types.NUMERIC => decimalConverter
                case java.sql.Types.TIMESTAMP => for {
                  timeStamp <- Option(rs.getTimestamp(i))
                  bytes <- Option(dateFormatter.format(timeStamp).getBytes())
                } yield bytes
                case java.sql.Types.DATE => for {
                  date <- Option(rs.getDate(i))
                  bytes <- Option(dateFormatter.format(date).getBytes())
                } yield bytes
                case java.sql.Types.TINYINT => Option(java.lang.Byte.toString(rs.getByte(i)).getBytes())
                case java.sql.Types.SMALLINT => Option(java.lang.Short.toString(rs.getShort(i)).getBytes())
                case java.sql.Types.INTEGER => Option(java.lang.Integer.toString(rs.getInt(i)).getBytes())
                case java.sql.Types.BIGINT => Option(java.lang.Long.toString(rs.getLong(i)).getBytes())
                case java.sql.Types.BOOLEAN => Option(java.lang.Boolean.toString(rs.getBoolean(i)).getBytes())
                case java.sql.Types.FLOAT => Option(java.lang.Double.toString(rs.getDouble(i)).getBytes())
                case java.sql.Types.DOUBLE => Option(java.lang.Double.toString(rs.getDouble(i)).getBytes())
                case java.sql.Types.REAL => Option(java.lang.Double.toString(rs.getDouble(i)).getBytes())
                case java.sql.Types.ROWID => for {
                  rowId <- Option(rs.getRowId(i))
                  bytes <- Option(rowId.toString().getBytes)
                } yield bytes
                case java.sql.Types.TIME => for {
                  time <- Option(rs.getTime(i))
                  bytes <- Option(time.toString().getBytes())
                } yield bytes
                case _ => stringConverter
              }
            } catch {
              case e: Throwable => None
            }
          }
          case _ => None
        }
        case None => None
      })
    } yield ret
  }

  def forM[A, B](iter: List[A])(proc: A => Operation[B]): Operation[List[B]] = iter match {
    case x :: xs => for {
      z <- proc(x)
      zs <- forM(xs)(proc)
      } yield (z :: zs)
    case Nil => operationPure(Nil)
  }

  def forM[A, B](iter: Queue[A])(proc: A => Operation[B]): Operation[Queue[B]] = iter match {
    case x +: xs => for {
      z <- proc(x)
      zs <- forM(xs)(proc)
    } yield (z +: zs)
    case _ => operationPure(Queue())
  }

  def forM[B](iter: Range.Inclusive)(proc: Int => Operation[B]): Operation[List[B]] =
    forM(iter.toList)(proc)
}
