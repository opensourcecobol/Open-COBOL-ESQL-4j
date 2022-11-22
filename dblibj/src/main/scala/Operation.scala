import cats.data.ContT
import cats.free.Free
import cats.free.Free.liftF
import cats.{Id, ~>}
import cats.data.StateT

import java.sql._
import java.util.Properties
import scala.collection.immutable.Queue
import org.postgresql.util.PSQLException

sealed trait ExecSuccessResult
case class EResultSet(resultSet: ResultSet) extends ExecSuccessResult
case class EUpdateCount(updateCount: Int) extends ExecSuccessResult
case class ESuccess() extends ExecSuccessResult

object Operation {
  type ExecResult = Either[SQLException, ExecSuccessResult]

  sealed trait CommandA[A]
  case class Connect(dbname: Option[String], host: Option[String], port: Option[String], user: Option[String],
                     passwd: Option[String], encoding: Option[String]) extends CommandA[Option[Connection]]
  case class Exec(addr: Connection, query: String) extends CommandA[ExecResult]
  case class ExecParam(addr: Connection, query: String, params: Queue[SQLVar]) extends CommandA[ExecResult]
  case class Close(addr: Connection) extends CommandA[Unit]
  case class SetAutoCommit(conn: Connection, autoCommit: Boolean) extends CommandA[Unit]
  case class GetEnvValue(key: String) extends CommandA[Option[String]]
  case class Log(msg: String) extends CommandA[Unit]
  case class ErrorLog(msg: String) extends CommandA[Unit]

  type Command[A] = Free[CommandA, A]

  // smart constructors
  def commandConnect(dbname: Option[String], host: Option[String], port: Option[String], user: Option[String],
                     passwd: Option[String], encoding: Option[String]): Command[Option[Connection]] =
    liftF[CommandA, Option[Connection]](Connect(dbname, host, port, user, passwd, encoding))

  def commandExec(addr: Connection, query: String): Command[ExecResult] =
    liftF[CommandA, ExecResult](Exec(addr, query))

  def commandExecParam(addr: Connection, query: String, param: Queue[SQLVar]): Command[ExecResult] =
    liftF[CommandA, ExecResult](ExecParam(addr, query, param))

  def commandSetAutoCommit(conn: Connection, autoCommit: Boolean): Command[Unit] =
    liftF[CommandA, Unit](SetAutoCommit(conn, autoCommit))

  def commandClose(addr: Connection): Command[Unit] =
    liftF[CommandA, Unit](Close(addr))

  def commandGetEnvValue(key: String): Command[Option[String]] =
    liftF[CommandA, Option[String]](GetEnvValue(key))

  def commandLog(msg: String): Command[Unit] =
    liftF(Log(msg))

  def commandErrorLog(msg: String): Command[Unit] =
    liftF(ErrorLog(msg))

  // utilities
  def commandLogLn(msg: String): Command[Unit] =
    commandLog(msg + System.lineSeparator())

  def commandErrorLogLn(msg: String): Command[Unit] =
    commandErrorLog(msg + System.lineSeparator())

  def commandGetEnvOrElse(key: Option[String], default: Option[String]): Command[Option[String]] =
    key match {
      case None => for {
          _ <- commandErrorLogLn("parameter is NULL")
        } yield default
      case Some(k) => for {
          value <- commandGetEnvValue(k)
          v <- value match {
            case None => for {
                _ <- commandErrorLogLn(s"param ${k} is not set. set default value. ")
              } yield default
            case Some(v) => Free.pure[CommandA, Option[String]](value)
          }
        } yield v
    }

  def commandGetEnvOrElse(key: String, default: String): Command[Option[String]] =
    commandGetEnvOrElse(Some(key), Some(default))

  def commandGetEnv(key: Option[String]): Command[Option[String]] =
    commandGetEnvOrElse(key, None)

  def commandGetEnv(key: String): Command[Option[String]] =
    commandGetEnvOrElse(Some(key), None)

  def compiler: CommandA ~> Id =
    new(CommandA ~> Id) {
      def apply[A](fa: CommandA[A]): Id[A] =
        fa match {
          case Connect(dbname, host, port, user, passwd, encoding) => {
            val _host = host.getOrElse("")
            val _port = port match {
              case None => ""
              case Some(p) => s":${p}"
            }
            val _dbname = dbname.getOrElse("")
            val url = s"jdbc:postgresql://${_host}${_port}/${_dbname}"
            val prop = new Properties()
            user.map(prop.put("user", _))
            passwd.map(prop.put("password", _))
            encoding.map(prop.put("encoding", _))
            try {
              Option(DriverManager.getConnection(url, prop))
            } catch {
              case e: Throwable => None
            }
          }
          case Exec(addr, query) => {
            try {
              val stmt = addr.createStatement()
              if(stmt.execute(query)) {
                Right(EResultSet(stmt.getResultSet()))
              } else {
                Right(EUpdateCount(stmt.getUpdateCount()))
              }
            } catch {
              case e: PSQLException => Left(e)
              case e: SQLException => Left(e)
              case e: Throwable => Left(new SQLException())
            }
          }
          case ExecParam(addr, query, params) => {
            try {
              val stmt = addr.prepareStatement(query)
              for ((param, i) <- params.zipWithIndex) {
                param.setParam(stmt, i + 1)
              }
              if(stmt.execute()) {
                Right(EResultSet(stmt.getResultSet()))
              } else {
                Right(EUpdateCount(stmt.getUpdateCount()))
              }
            } catch {
              case e: PSQLException => Left(e)
              case e: SQLException => Left(e)
              case e: Throwable => Left(new SQLException())
            }
          }
          case SetAutoCommit(conn, autoCommit) =>
            conn.setAutoCommit(autoCommit)
          case Close(addr) => {
            addr.close()
          }
          case GetEnvValue(key: String) =>
            Option(System.getenv(key))
          case Log(msg) =>
            getLogLevel() match {
              case LogOutputDebug() =>
                writeLog(msg, getLogFilePath())
              case _ => ()
            }
          case ErrorLog(msg) =>
            getLogLevel() match {
              case LogOutputError() =>
                writeLog(msg, getLogFilePath())
              case LogOutputDebug() =>
                writeLog(msg, getLogFilePath())
              case _ => ()
            }
        }
    }

  sealed trait LogLevel
  case class LogOutputNoTest() extends  LogLevel
  case class LogOutputNothing() extends LogLevel
  case class LogOutputError() extends LogLevel
  case class LogOutputDebug() extends LogLevel

  def getLogLevel(): LogLevel =
    Option(System.getenv("OCDB_LOGLEVEL")) match {
      case Some(envValue) if(envValue.toLowerCase == "nolog") => LogOutputNothing()
      case Some(envValue) if(envValue.toLowerCase == "err") => LogOutputError()
      case Some(envValue) if(envValue.toLowerCase == "debug") => LogOutputDebug()
      case _ => LogOutputNothing()
    }

  def getLogFilePath(): String =
    Option(System.getenv("OCDB_LOGFILE")) match {
      case Some(path) => path
      case _ => "/tmp/ocesql.log"
    }

  def writeLog(msg: String, filePath: String): Unit = {
    val writer = new java.io.FileWriter(filePath, true)
    try {
      writer.write(msg)
    } finally {
      writer.close()
    }
  }

  def when[A](condition: Boolean, proc: Command[A]): Command[Unit] =
    if(condition) {
      for {
        _ <- proc
      } yield ()
    } else {
      for {
        _ <- Free.pure[CommandA, Unit](())
      } yield ()
    }

  type Operation[A] = StateT[Command, OCDBState, A]
  def operationPure[A](x: A): Operation[A] = StateT.pure[Command, OCDBState, A](x)

  def connect(dbname: Option[String], host: Option[String], port: Option[String], user: Option[String],
              passwd: Option[String], encoding: Option[String]): Operation[Option[Connection]] =
    StateT.liftF(commandConnect(dbname, host, port, user, passwd, encoding))
  def exec(addr: Connection, query: String): Operation[ExecResult] =
    StateT.liftF(commandExec(addr, query))
  def execParam(addr: Connection, query: String, params: Queue[SQLVar]): Operation[ExecResult] =
    StateT.liftF(commandExecParam(addr, query, params))
  def setAutoCommit(conn: Connection, autoCommit: Boolean): Operation[Unit] =
    StateT.liftF(commandSetAutoCommit(conn, autoCommit))
  def close(addr: Connection): Operation[Unit] =
    StateT.liftF(commandClose(addr))
  def getEnvValue(key: String): Operation[Option[String]] =
    StateT.liftF(commandGetEnvValue(key))
  def log(msg: String): Operation[Unit] =
    StateT.liftF(commandLog(msg))
  def errorLog(msg: String): Operation[Unit] =
    StateT.liftF(commandErrorLog(msg))
  def logLn(msg: String): Operation[Unit] =
    StateT.liftF(commandLogLn(msg))
  def errorLogLn(msg: String): Operation[Unit] =
    StateT.liftF(commandErrorLogLn(msg))
  def comGetEnvOrElse(key: Option[String], default: Option[String]): Operation[Option[String]] =
    StateT.liftF(commandGetEnvOrElse(key, default))
  def comGetEnvOrElse(key: String, default: String): Operation[Option[String]] =
    StateT.liftF(commandGetEnvOrElse(key, default))
  def comGetEnv(key: Option[String]): Operation[Option[String]] =
    StateT.liftF(commandGetEnv(key))
  def comGetEnv(key: String): Operation[Option[String]] =
    StateT.liftF(commandGetEnv(key))

  // state operation
  def getState: Operation[OCDBState] =
    StateT.get[Command, OCDBState]

  def setState(state: OCDBState): Operation[Unit] =
    StateT.set[Command, OCDBState](state)

  def updateState(f: OCDBState => OCDBState): Operation[Unit] = for {
    state <- getState
    _ <- setState(f(state))
  } yield ()

  def when[A](condition: Boolean, proc: Operation[A]): Operation[Unit] =
    if(condition) {
      for {
        _ <- proc
      } yield ()
    } else {
      for {
        _ <- operationPure(())
      } yield ()
    }

  type OperationC[B, A] = ContT[Operation, B, A]

  def whenExecuteAndExit[B](cond: Boolean, proc: Operation[B]): OperationC[B, Unit] =
    ContT[Operation, B, Unit]{ next  =>
      if (cond) {
        proc
      } else {
        next(())
      }
    }

  def whenExecute[B](cond: Boolean, proc: Operation[Unit]): OperationC[B, Unit] =
    ContT[Operation, B, Unit]{ next =>
      if(cond) {
        proc.flatMap(next)
      } else {
        next(())
      }
    }

  def operationCPure[B, A](action: Operation[A]): OperationC[B, A] =
    ContT[Operation, B, A] { next => action.flatMap(next) }

  def operationCCPure[B, A](action: A): OperationC[B, A] =
    operationCPure(operationPure(action))

  // for Continuation
  def connectC[B](dbname: Option[String], host: Option[String], port: Option[String], user: Option[String],
                  passwd: Option[String], encoding: Option[String]): OperationC[B, Option[Connection]] =
    operationCPure(connect(dbname, host, port, user, passwd, encoding))
  def execC[B](addr: Connection, query: String): OperationC[B, ExecResult] =
    operationCPure(exec(addr, query))
  def closeC[B](addr: Connection): OperationC[B, Unit] =
    operationCPure(close(addr))
  def getEnvValueC[B](key: String): OperationC[B, Option[String]] =
    operationCPure(getEnvValue(key))
  def logC[B](msg: String): OperationC[B, Unit] =
    operationCPure(log(msg))
  def errorLogC[B](msg: String): OperationC[B, Unit] =
    operationCPure(errorLog(msg))
  def logLnC[B](msg: String): OperationC[B, Unit] =
    operationCPure(logLn(msg))
  def errorLogLnC[B](msg: String): OperationC[B, Unit] =
    operationCPure(errorLog(msg))
  def comGetEnvOrElseC[B](key: Option[String], default: Option[String]): OperationC[B, Option[String]] =
    operationCPure(comGetEnvOrElse(key, default))
  def comGetEnvOrElseC[B](key: String, default: String): OperationC[B, Option[String]] =
    operationCPure(comGetEnvOrElse(key, default))
  def comGetEnvC[B](key: Option[String]): OperationC[B, Option[String]] =
    operationCPure(comGetEnv(key))
  def comGetEnvC[B](key: String): OperationC[B, Option[String]] =
    operationCPure(comGetEnv(key))

  // state operation
  def getStateC[B]: OperationC[B, OCDBState] =
    ContT.liftF[Operation, B, OCDBState](getState)

  def setStateC[B](state: OCDBState): OperationC[B, Unit] =
    ContT.liftF[Operation, B, Unit](setState(state))

  def OperationCPure[B, A](x: A): OperationC[B, A] =
    ContT.pure(x)
}