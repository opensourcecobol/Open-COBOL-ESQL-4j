import ConstValues._
import Operation._
import Common._

import java.sql._

object OCESQLConnectCore {
  val OCESQL_DEFAULT_DBNAME = "OCDB_DEFAULT_DBNAME"

  def connect(user: Option[String], passwd: Option[String], name: Option[String], atdb: Option[String]): Operation[Int] = {
    val tmpName   = name.map(getStrWithoutAfterSpace(_))
    val tmpUser   = user.map(getStrWithoutAfterSpace(_))
    val tmpPasswd = passwd.map(getStrWithoutAfterSpace(_))

    for {
      dbName <- getNameWithEnvValue(tmpName, "OCDB_DB_NAME")
      dbUser <- getNameWithEnvValue(tmpUser, "OCDB_DB_USER")
      dbPasswd <- getNameWithEnvValue(tmpPasswd, "OCDB_DB_PASS")
      result <- connectMain(dbName, dbUser, dbPasswd, atdb.getOrElse(OCESQL_DEFAULT_DBNAME))
    } yield result
  }

  def getStrWithoutAfterSpace(str: String): String = {
    val spaceIndex = str.indexOf(' ')
    if (spaceIndex <= 0) {
      str
    } else {
      str.substring(0, spaceIndex)
    }
  }

  private def getNameWithEnvValue(defaultName: Option[String], envKey: String): Operation[Option[String]] =
    defaultName match {
      case None => comGetEnv(envKey)
      case Some(name) => operationPure(defaultName)
    }

  private def connectMain(name: Option[String], user: Option[String], passwd: Option[String], connDBName: String): Operation[Int] = {
    val dbname = name
    val dbType = DB_Postgres()
    val st = SqlCA.defaultValue
    val connStr = getConnectionStr(dbname, user, passwd)
    val (real_dbname, host, port) = parseDBName(dbname)
    val autoCommit = true

    (for {
      cEncoding <- comGetEnvOrElseC("OCDB_DB_CHAR", "SJIS")

      _ <- logLnC("dbname   = " + name.getOrElse(""))
      _ <- logLnC("user     = " + user.getOrElse(""))
      _ <- logLnC("password = " + passwd.getOrElse(""))
      _ <- logLnC("connname = " + connDBName)

      state <- getStateC

      _ <- whenExecuteAndExit(!checkType(dbType),
        for {
          _ <- errorLogLn(s"dbtype invalid")
          _ <- setState(state.setSqlCA(st.setCode(OCDB_CONN_FAIL_CONNECT)))
        } yield 1)

      id <- OperationCPure(resolveCONNID(connDBName, state.globalState.connectionMap))
      connectId <- operationCPure(connectDB(dbType, real_dbname, host, port, user, passwd,
        connDBName, autoCommit, cEncoding))

      _ <- logLnC(s"publish connect Id: ${connectId}")

      _ <- whenExecuteAndExit(connectId == OCDB_CONN_FAIL_CONNECT, for {
        _ <- setLibErrorStatus(OCDB_CONNECT())
        _ <- errorLogLn(s"connection failed. connect param is :${connStr}")
      } yield 1)

      _ <- whenExecuteAndExit(connectId == INVALID_CONN_ID, for {
        _ <- setLibErrorStatus(OCDB_CONNECT())
        _ <- errorLogLn(s"connection failed. connect param is :${connStr}")
      } yield 1)

      //[remark] トランザクションがどこでとじられるのか不明のため一時的に消去
      //_ <- operationCPure(OCDBExec(connectId, "BEGIN"))

      resultStatusFlag <- operationCPure(setResultStatus(connectId))
      _ <- whenExecuteAndExit(resultStatusFlag,
        operationPure(1))

      _ <- logLnC(s"Connection success. connectId = ${connectId}, dbname=")
    } yield 0).eval
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
                        passwd: Option[String], connName: String, autoCommit: Boolean, encoding: Option[String]): Operation[Int] =
    (for {
      connAddr <- operationCPure(OCDB_PGConnect(dbname, host, port, user, passwd, autoCommit, encoding))

      //[remark] temporary implementation
      //_ <- whenExecuteAndExit(connAddr == OCDB_CONN_FAIL_CONNECT,
      _ <- whenExecuteAndExit(connAddr.isEmpty,
        operationPure(OCDB_CONN_FAIL_CONNECT))

      returnValue <- operationCPure(addConnList(dbType, connName, connAddr))

      _ <- whenExecuteAndExit(returnValue == INVALID_CONN_ID, for {
          _ <- OCDB_PGFinish(connAddr)
        } yield INVALID_CONN_ID)

      _ <- logLnC(s"connid=${returnValue}")
    } yield returnValue).eval

  private def addConnList(dbType: DBtype, connName: String, connAddr: Option[Connection]): Operation[Int] = {
    val actionAddConnectionToMap = {
      val newConnection = createConn(dbType, connName, connAddr)
      for {
        _ <- addConnectionInfoToMap(connName, newConnection)
      } yield newConnection.id
    }

    for {
      state <- getState
      result <- state.globalState.connectionMap.get(connName) match {
        case None => actionAddConnectionToMap
        case Some(sc) => if (connName == sc.cid) {
          for {
            _ <- errorLogLn(s"connection id ${connName} is already registered")
          } yield INVALID_CONN_ID
        } else {
          actionAddConnectionToMap
        }
      }
    } yield result
  }

  private def addConnectionInfoToMap(key: String, info: ConnectionInfo): Operation[Unit] = for {
    state <- getState
    _ <- setState({
      val connectionMap = state.globalState.connectionMap
      val newConnectionMap = connectionMap + (key -> info)
      state.setGlobalState(state.globalState.setConnectionMap(newConnectionMap))
    })
  } yield ()

  private def createConn(dbType: DBtype, connName: String, connAddr: Option[Connection]): ConnectionInfo =
    new ConnectionInfo(
      DEFAULT_NEXT_CONN_ID,
      connName,
      dbType,
      connAddr,
      None,
      Left(new SQLException()),
      Some(""),
      Some("")
    )

  private def setRollBackOneMode(flag: Boolean): Operation[Unit] = for {
    state <- getState
    _ <- setState({
      val globalState = state.globalState
      val newGlobalState = globalState.setRollBackOneMode(flag)
      state.setGlobalState(newGlobalState)
    })
  } yield ()

  private def OCDB_PGConnect(dbname: Option[String], host: Option[String], port: Option[String],user: Option[String],
                             passwd: Option[String], autoCommit: Boolean, encoding: Option[String]): Operation[Option[Connection]] =
    Operation.connect(dbname, host, port, user, passwd, encoding).flatMap(_ match {
      case None => operationPure(None)
      case Some(c) => for {
        _ <- setAutoCommit(c, autoCommit)
        _ <- comGetEnv("OCDB_PG_IGNORE_ERROR").flatMap(_ match  {
          case None => setRollBackOneMode(false)
          case Some(envValue) => setRollBackOneMode(envValue == "Y")
        })
      } yield Some(c)
    })

  def connectInformal(connInfo: Option[String], atdb: Option[String]): Operation[Int] = {
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


    val errorProc = for {
      _ <- errorLogLn("Connection information is NULL")
    } yield -1

    connInfoBuf match {
      case None => errorProc
      case Some(ci) if ci.isEmpty => errorProc
      case Some(ci) => for {
        _ <- logLn(ci)
        dbName <- getNameWithEnvValue(tmpName, "OCDB_DB_NAME")
        dbUser <- getNameWithEnvValue(tmpUser, "OCDB_DB_USER")
        dbPasswd <- getNameWithEnvValue(tmpPasswd, "OCDB_DB_PASS")
        returnValue <- connectMain(dbName, dbUser, dbPasswd, atdb.getOrElse(OCESQL_DEFAULT_DBNAME))
      } yield returnValue
    }
  }
}