import ConstValues._
import Operation._
import Common._
import java.util.Properties

import java.sql._

object OCESQLConnectCore {
  val OCESQL_DEFAULT_DBNAME = "OCDB_DEFAULT_DBNAME"

  def connect(user: Option[String], passwd: Option[String], name: Option[String], atdb: Option[String], state: OCDBState): Int = {
    val tmpName   = name.map(getStrWithoutAfterSpace(_))
    val tmpUser   = user.map(getStrWithoutAfterSpace(_))
    val tmpPasswd = passwd.map(getStrWithoutAfterSpace(_))

    val dbName = getNameWithEnvValue(tmpName, "OCDB_DB_NAME")
    val dbUser = getNameWithEnvValue(tmpUser, "OCDB_DB_USER")
    val dbPasswd = getNameWithEnvValue(tmpPasswd, "OCDB_DB_PASS")
    connectMain(dbName, dbUser, dbPasswd, atdb.getOrElse(OCESQL_DEFAULT_DBNAME), state)
  }

  def getStrWithoutAfterSpace(str: String): String = {
    val spaceIndex = str.indexOf(' ')
    if (spaceIndex <= 0) {
      str
    } else {
      str.substring(0, spaceIndex)
    }
  }

  private def getNameWithEnvValue(defaultName: Option[String], envKey: String): Option[String] =
    defaultName match {
      case None => Option(System.getenv(envKey))
      case Some(name) => defaultName
    }

  private def connectMain(name: Option[String], user: Option[String], passwd: Option[String], connDBName: String, state: OCDBState): Int = {
    val dbname = name
    val dbType = DB_Postgres()
    val st = SqlCA.defaultValue
    val connStr = getConnectionStr(dbname, user, passwd)
    val (real_dbname, host, port) = parseDBName(dbname)
    val autoCommit = true

    val cEncoding = Option(System.getenv("OCDB_DB_CHAR")).getOrElse("UTF-8")

    logLn("dbname   = " + name.getOrElse(""))
    logLn("user     = " + user.getOrElse(""))
    logLn("password = " + passwd.getOrElse(""))
    logLn("connname = " + connDBName)

    if(!checkType(dbType)) {
      errorLogLn(s"dbtype invalid")
      state.updateSQLCA(st.setCode(OCDB_CONN_FAIL_CONNECT))
      return 1
    }

    val id = resolveCONNID(connDBName, state.globalState.connectionMap)
    val connectId = connectDB(dbType, real_dbname, host, port, user, passwd,
      connDBName, autoCommit, cEncoding, state)

    logLn(s"publish connect Id: ${connectId}")

    if(connectId == OCDB_CONN_FAIL_CONNECT) {
      setLibErrorStatus(OCDB_CONNECT(), state)
      errorLogLn(s"connection failed. connect param is :${connStr}")
      return 1
    }

    if(connectId == INVALID_CONN_ID) {
      setLibErrorStatus(OCDB_CONNECT(), state)
      errorLogLn(s"connection failed. connect param is :${connStr}")
      return 1
    }

    //[remark] トランザクションがどこでとじられるのか不明のため一時的に消去
    OCDBExec(connectId, "BEGIN", state)

    if(setResultStatus(connectId, state)) {
      return 1
    }

    logLn(s"Connection success. connectId = ${connectId}, dbname=")
    0
  }


  private def checkType(dbType: DBtype): Boolean = true

  private def getConnectionStr(dbname: Option[String], dbuser: Option[String], dbpasswd: Option[String]): String = {
    val (real_dbname, dbhost, dbport) = parseDBName(dbname)
    getConnectInformationString(real_dbname, dbhost, dbport, dbuser, dbpasswd)
  }

  private def parseDBName(dbname: Option[String]): (Option[String], Option[String], Option[String]) =
    dbname match {
      case None => (None, None, None)
      case Some(str) => {
        val (_dbname, dbport) = splitString(':', str)
        val (real_dbname, dbhost) = splitString('@', _dbname)
        (Some(real_dbname), dbhost, dbport)
      }
    }

  private def getConnectInformationString(
                                           dbname:   Option[String],
                                           dbhost:   Option[String],
                                           dbport:   Option[String],
                                           dbuser:   Option[String],
                                           dbpasswd: Option[String]): String = {
    val dbname_info:   String = dbname.map(s => s"name=${s}").getOrElse("")
    val dbhost_info:   String = dbhost.map(s => s"host=${s}").getOrElse("")
    val dbport_info:   String = dbport.map(s => s"port=${s}").getOrElse("")
    val dbuser_info:   String = dbuser.map(s => s"user=${s}").getOrElse("")
    val dbpasswd_info: String = dbpasswd.map(s => s"password=${s}").getOrElse("")
    s"${dbname_info} ${dbhost_info} ${dbport_info} ${dbport_info} ${dbuser_info} ${dbpasswd_info}"
  }

  private def splitString(separator: Char, str: String): (String, Option[String]) = {
    val index = str.lastIndexOf(separator)
    if(index >= 0) {
      (str.substring(0, index), Some(str.substring(index + 1, str.length)))
    } else {
      (str, None)
    }
  }

  /**
   * Equivalent to OCDBConnect in dblib/ocdb.c
   * @param dbType
   * @param connInfo
   * @param connName
   * @param autoCommit
   * @param cEncoding
   * @return
   */
  private def connectDB(dbType: DBtype, dbname: Option[String], host: Option[String], port: Option[String], user: Option[String],
                        passwd: Option[String], connName: String, autoCommit: Boolean, encoding: String, state: OCDBState): Int = {
    val connAddr = OCDB_PGconnect(dbname, host, port, user, passwd, autoCommit, encoding, state)

    //[remark] temporary implementation
    //_ <- whenExecuteAndExit(connAddr == OCDB_CONN_FAIL_CONNECT,
    if(connAddr.isEmpty) {
      return OCDB_CONN_FAIL_CONNECT
    }

    val returnValue = addConnList(dbType, connName, connAddr, state)

    if(returnValue == INVALID_CONN_ID) {
      OCDB_PGFinish(connAddr, state)
      return INVALID_CONN_ID
    }

    logLn(s"connid=${returnValue}")
    returnValue
  }

  private def addConnList(dbType: DBtype, connName: String, connAddr: Option[Connection], state: OCDBState): Int = {
    def actionAddConnectionToMap() = {
      val newConnection = createConn(dbType, connName, connAddr)
      addConnectionInfoToMap(connName, newConnection, state)
      newConnection.id
    }

    state.globalState.connectionMap.get(connName) match {
      case None => actionAddConnectionToMap()
      case Some(sc) => if (connName == sc.cid) {
        errorLogLn(s"connection id ${connName} is already registered")
        INVALID_CONN_ID
      } else {
        actionAddConnectionToMap()
      }
    }
  }

  private def addConnectionInfoToMap(key: String, info: ConnectionInfo, state: OCDBState): Unit = {
    val connectionMap = state.globalState.connectionMap
    val newConnectionMap = connectionMap + (key -> info)
    state.updateGlobalState(state.globalState.setConnectionMap(newConnectionMap))
  }

  private def createConn(dbType: DBtype, connName: String, connAddr: Option[Connection]): ConnectionInfo =
    new ConnectionInfo(
      DEFAULT_NEXT_CONN_ID,
      connName,
      dbType,
      connAddr,
      None,
      Right(ESuccess()),
      Some(""),
      Some("")
    )

  private def setRollBackOneMode(flag: Boolean, state: OCDBState): Unit = {
    val globalState = state.globalState
    val newGlobalState = globalState.setRollBackOneMode(flag)
    state.updateGlobalState(newGlobalState)
  }

  private def OCDB_PGconnect(dbname: Option[String], host: Option[String], port: Option[String],user: Option[String],
                             passwd: Option[String], autoCommit: Boolean, encoding: String, state: OCDBState): Option[Connection] =
    execute_connect(dbname, host, port, user, passwd, encoding, state) match {
      case None => None
      case Some(c) => {
        c.setAutoCommit(autoCommit)
        Option(System.getenv("OCDB_PG_IGNORE_ERROR")) match  {
          case None => setRollBackOneMode(false, state)
          case Some(envValue) => setRollBackOneMode(envValue == "Y", state)
        }
        Some(c)
      }
    }

  private def execute_connect(dbname: Option[String], host: Option[String], port: Option[String],user: Option[String],
                           passwd: Option[String], encoding: String, state: OCDBState): Option[Connection] = {
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
    prop.put("encoding", encoding)
    try {
      Option(DriverManager.getConnection(url, prop))
    } catch {
      case e: Throwable => None
    }
  }

  def connectInformal(connInfo: Option[String], atdb: Option[String], state: OCDBState): Int = {
    val _connInfoBuf = connInfo.map(getStrWithoutAfterSpace(_))

    val _dsn = _connInfoBuf.map(splitString('@', _))

    val dsn = _dsn.map(_._2).getOrElse(None)
    val dsnRest = _dsn.map(_._1)

    val _passwd = dsnRest.map(splitString('/', _))

    val passwd = _passwd.map(_._2).getOrElse(None)
    val connInfoBuf = _passwd.map(_._1)

    val tmpName = dsn.map(getStrWithoutAfterSpace(_))
    val tmpUser = connInfoBuf.map(getStrWithoutAfterSpace(_))
    val tmpPasswd = passwd.map(getStrWithoutAfterSpace(_))


    def errorProc = {
      errorLogLn("Connection information is NULL")
      -1
    }

    connInfoBuf match {
      case None => errorProc
      case Some(ci) if ci.isEmpty => errorProc
      case Some(ci) => {
        logLn(ci)
        val dbName = getNameWithEnvValue(tmpName, "OCDB_DB_NAME")
        val dbUser = getNameWithEnvValue(tmpUser, "OCDB_DB_USER")
        val dbPasswd = getNameWithEnvValue(tmpPasswd, "OCDB_DB_PASS")
        connectMain(dbName, dbUser, dbPasswd, atdb.getOrElse(OCESQL_DEFAULT_DBNAME), state)
      }
    }
  }
}