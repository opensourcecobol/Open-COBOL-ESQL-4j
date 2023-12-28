import java.sql._

sealed trait ExecSuccessResult
case class EResultSet(resultSet: ResultSet) extends ExecSuccessResult
case class EUpdateCount(updateCount: Int) extends ExecSuccessResult
case class ESuccess() extends ExecSuccessResult

object Operation {
  type ExecResult = Either[SQLException, ExecSuccessResult]

  sealed trait LogLevel
  case class LogOutputNoTest() extends LogLevel
  case class LogOutputNothing() extends LogLevel
  case class LogOutputError() extends LogLevel
  case class LogOutputDebug() extends LogLevel

  def isLoggingEnable(): Boolean = {
    getLogLevel() match {
      case LogOutputDebug() => true
      case _                => false
    }
  }

  def log(msg: String): Unit = {
    if (isLoggingEnable()) {
      writeLog(msg, getLogFilePath())
    }
  }

  def logLn(msg: String): Unit = log(msg + "\n")

  def errorLog(msg: String): Unit = {
    getLogLevel() match {
      case LogOutputError() =>
        writeLog(msg, getLogFilePath())
      case LogOutputDebug() =>
        writeLog(msg, getLogFilePath())
      case _ => ()
    }
  }

  def errorLogLn(msg: String): Unit = errorLog(msg + "\n")

  def getLogLevel(): LogLevel =
    Option(System.getenv("OCDB_LOGLEVEL")) match {
      case Some(envValue) if (envValue.toLowerCase == "nolog") =>
        LogOutputNothing()
      case Some(envValue) if (envValue.toLowerCase == "err") => LogOutputError()
      case Some(envValue) if (envValue.toLowerCase == "debug") =>
        LogOutputDebug()
      case _ => LogOutputNothing()
    }

  def getLogFilePath(): String =
    Option(System.getenv("OCDB_LOGFILE")) match {
      case Some(path) => path
      case _          => "/tmp/ocesql.log"
    }

  def writeLog(msg: String, filePath: String): Unit = {
    val writer = new java.io.FileWriter(filePath, true)
    try {
      writer.write(msg)
    } finally {
      writer.close()
    }
  }
}
