import java.sql._
import Operation.ExecResult

class ConnectionInfo(
    val id: Int,
    val cid: String,
    val dbType: DBtype,
    val connAddr: Option[Connection],
    val resAddr: Option[Connection],
    val result: ExecResult,
    val errorMessage: Option[String],
    val pid: Option[String]
) {
  def setId(id: Int): ConnectionInfo = new ConnectionInfo(
    id,
    cid,
    dbType,
    connAddr,
    resAddr,
    result,
    errorMessage,
    pid
  )
  def setCid(cid: String): ConnectionInfo = new ConnectionInfo(
    id,
    cid,
    dbType,
    connAddr,
    resAddr,
    result,
    errorMessage,
    pid
  )
  def setDbType(dbType: DBtype): ConnectionInfo = new ConnectionInfo(
    id,
    cid,
    dbType,
    connAddr,
    resAddr,
    result,
    errorMessage,
    pid
  )
  def setConnAddr(connAddr: Option[Connection]): ConnectionInfo = new ConnectionInfo(
    id,
    cid,
    dbType,
    connAddr,
    resAddr,
    result,
    errorMessage,
    pid
  )
  def setResAddr(resAddr: Option[Connection]): ConnectionInfo = new ConnectionInfo(
    id,
    cid,
    dbType,
    connAddr,
    resAddr,
    result,
    errorMessage,
    pid
  )
  def setResult(result: ExecResult): ConnectionInfo = new ConnectionInfo(
    id,
    cid,
    dbType,
    connAddr,
    resAddr,
    result,
    errorMessage,
    pid
  )
  def setErrorMessage(errorMessage: Option[String]): ConnectionInfo = new ConnectionInfo(
    id,
    cid,
    dbType,
    connAddr,
    resAddr,
    result,
    errorMessage,
    pid
  )
  def setPid(pid: Option[String]): ConnectionInfo = new ConnectionInfo(
    id,
    cid,
    dbType,
    connAddr,
    resAddr,
    result,
    errorMessage,
    pid
  )
}

object ConnectionInfo {
  type ConnectionMap = scala.collection.immutable.Map[String, ConnectionInfo]
  def defaultValue: ConnectionInfo = new ConnectionInfo(
    0,
    "",
    DB_Postgres(),
    None,
    None,
    Left(new SQLException()),
    Some(""),
    Some("")
  )
  def emptyConnectionMap: ConnectionMap = Map()
}
