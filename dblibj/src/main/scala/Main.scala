import jp.osscons.opensourcecobol.libcobj.call.CobolRunnable
import jp.osscons.opensourcecobol.libcobj.data.CobolDataStorage
import scala.collection.immutable._
import java.nio.ByteBuffer
import Operation._
import Common._
import ConstValues._
import SQLVar._
import Select._
import Cursor._
import Prepare._

trait CobolRunnableWrapper extends CobolRunnable {
  override def cancel(): Unit = {}
  override def isActive(): Boolean = true
  override def run(args: CobolDataStorage*): Int = {
    val initialState: OCDBState = if (parseSqlCA) {
      OCDBState.initialState(args(0))
    } else {
      new OCDBState(SqlCA.defaultValue, Common.internalState)
    }
    val (newOCBDState, result) = execute(args).run(initialState).foldMap(compiler)
    if(parseSqlCA) {
      OCDBState.updateByState(args(0), newOCBDState)
    } else {
      OCDBState.updateByState(newOCBDState)
    }
    Common.internalState = newOCBDState.globalState
    result
  }

  var parseSqlCA: Boolean = true

  def execute(args: Seq[CobolDataStorage]): Operation[Int]

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

  def storageToString(stringStorage: CobolDataStorage, lengthStorage: CobolDataStorage): Option[String] = {
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
  override def execute(args: Seq[CobolDataStorage]): Operation[Int] = {
    val user  : Option[String] = storageToString(args(1), args(2))
    val passwd: Option[String] = storageToString(args(3), args(4))
    val name  : Option[String] = storageToString(args(5), args(6))
    for {
      _ <- logLn("OCESQLConnect start")
      result <- OCESQLConnectCore.connect(user, passwd, name, None)
    } yield result
  }
}

class OCESQLIDConnect extends CobolRunnableWrapper {
  override def execute(args: Seq[CobolDataStorage]): Operation[Int] = {
    val _atdb  : Option[String] = storageToString(args(1), args(2))
    val user  : Option[String] = storageToString(args(3), args(4))
    val passwd: Option[String] = storageToString(args(5), args(6))
    val name  : Option[String] = storageToString(args(7), args(8))
    val atdb = _atdb.map(OCESQLConnectCore.getStrWithoutAfterSpace(_))

    val errorProc = for {
      _ <- setLibErrorStatus(OCDB_VAR_NOT_CHAR())
    } yield 1

    for {
      _ <- logLn("OCESQLIDConnect start")
      result <- atdb match {
        case None => errorProc
        case Some(a) => if (a.isEmpty) {
          errorProc
        } else {
          OCESQLConnectCore.connect(user, passwd, name, atdb)
        }
      }
    } yield result
  }
}

class OCESQConnectShort extends CobolRunnableWrapper {
  override def execute(args: Seq[CobolDataStorage]): Operation[Int] = for {
    _ <- logLn("OCESQLConnectShort start")
    returnValue <- OCESQLConnectCore.connect(None, None, None, None)
  } yield returnValue
}

class OCESQIDConnectShort extends CobolRunnableWrapper {
  override def execute(args: Seq[CobolDataStorage]): Operation[Int] = {
    val _atdb  : Option[String] = storageToString(args(1), args(2))
    val atdb = _atdb.map(OCESQLConnectCore.getStrWithoutAfterSpace(_))
    for {
      _ <- logLn("OCESQLIDConnectShort start")
      returnValue <- OCESQLConnectCore.connect(None, None, None, atdb)
    } yield returnValue
  }
}

class OCESQLConnectInformal extends CobolRunnableWrapper {
  override def execute(args: Seq[CobolDataStorage]): Operation[Int] = {
    val connInfo = storageToString(args(1), args(2))
    for {
      _ <- logLn("OCESQLConnectInformal start")
      returnValue <- OCESQLConnectCore.connectInformal(connInfo, None)
    } yield returnValue
  }
}

class OCESQLIDConnectInformal extends CobolRunnableWrapper {
  override def execute(args: Seq[CobolDataStorage]): Operation[Int] = {
    val atdb = storageToString(args(1), args(2))
    val connInfo = storageToString(args(3), args(4))
    for {
      _ <- logLn("OCESQLIDConnectInformal start")
      returnValue <- OCESQLConnectCore.connectInformal(connInfo, atdb)
    } yield returnValue
  }
}

class OCESQLPrepare extends CobolRunnableWrapper {
  override def execute(args: Seq[CobolDataStorage]): Operation[Int] = {
    val sname = getCString(args(1)).getOrElse("")
    val query = parsePrepareQuery(args(2), args(3))

    val nParams = "\\?".r.findAllIn(query).length

    for {
      _ <- logLn(s"Add prepare sname:${sname}, nParams:${nParams}, query:'${query}'")
      _ <- addQueryInfoMap(sname, query, nParams)
    } yield 0
  }
}

class OCESQLDisconnect extends CobolRunnableWrapper {
  override def execute(args: Seq[CobolDataStorage]): Operation[Int] =
    resolveCONNID(OCESQL_DEFAULT_DB_NAME).flatMap(_ match {
      case None => for {
        _ <- errorLogLn("connection id is not found.")
        _ <- setLibErrorStatus(OCDB_NO_CONN())
      } yield 1
      case Some(c) => for {
        _ <- OCESQLDisconnectCore.disconnect(c.id)
      } yield 0
    })
}

class OCESQLIDDisconnect extends CobolRunnableWrapper {
  override def execute(args: Seq[CobolDataStorage]): Operation[Int] = {
    val atdb = storageToString(args(1), args(2))
    resolveCONNID(atdb.getOrElse("")).flatMap(_ match {
      case None => for {
        _ <- errorLogLn("connection id is not found.")
        _ <- setLibErrorStatus(OCDB_NO_CONN())
      } yield 1
      case Some(c) => for {
        _ <- OCESQLDisconnectCore.disconnect(c.id)
      } yield 0
    })
  }
}

class OCESQLExec extends CobolRunnableWrapper {
  override def execute(args: Seq[CobolDataStorage]): Operation[Int] = {
    val query = getCString(args(1))
    for {
      _ <- logLn("OCESQLExec start")
      _ <- logLn(s"SQL:#${query}#")
      returnValue <- resolveCONNID(OCESQL_DEFAULT_DB_NAME).flatMap(_ match {
        case None => for {
          _ <- errorLogLn("connection id is not found")
          _ <- setLibErrorStatus(OCDB_NO_CONN())
          } yield 1
        case Some(connInfo) =>for {
          _ <- ocesqlExec(connInfo.id, query)
          } yield 0
      })
    } yield returnValue
  }
}

class OCESQLIDExec extends CobolRunnableWrapper {
  override def execute(args: Seq[CobolDataStorage]): Operation[Int] = {
    val atdb = storageToString(args(1), args(2))
    val query = getCString(args(3))
    for {
      _ <- logLn("OCESQLIDExec start")
      _ <- logLn(s"SQL:#${query}#")
      returnValue <- resolveCONNID(atdb.getOrElse("")).flatMap(_ match {
        case None => for {
          _ <- errorLogLn("connection id is not found")
          _ <- setLibErrorStatus(OCDB_NO_CONN())
        } yield 1
        case Some(connInfo) =>for {
          _ <- ocesqlExec(connInfo.id, query)
        } yield 0
      })
    } yield returnValue
  }
}

class OCESQLExecParams extends CobolRunnableWrapper {
  override def execute(args: Seq[CobolDataStorage]): Operation[Int] = {
    val query = getCString(args(1))
    val nParams = storageToInt(args(2))
    for {
      _ <- logLn("OCESQLExecParams start")
      _ <- logLn(s"SQL:#${query.getOrElse("")}#")
      id <- resolveCONNID(OCESQL_DEFAULT_DB_NAME)
      returnCode <- id match {
        case None => for {
          _ <- errorLogLn("connection id is not found")
          _ <- setLibErrorStatus(OCDB_NO_CONN())
        } yield 1
        case Some(connectionInfo) => for {
          _ <- ocesqlExecParams(connectionInfo.id, query, nParams.getOrElse(0))
        } yield 0
      }
    } yield returnCode
  }
}

class OCESQLIDExecParams extends CobolRunnableWrapper {
  override def execute(args: Seq[CobolDataStorage]): Operation[Int] = {
    val atdb = storageToString(args(1), args(2))
    val query = getCString(args(3))
    val nParams = storageToInt(args(4))
    for {
      _ <- logLn("OCESQLIDExecParams start")
      id <- resolveCONNID(atdb.getOrElse(""))
      returnCode <- id match {
        case None => for {
          _ <- errorLogLn("connection id is not found")
          _ <- setLibErrorStatus(OCDB_NO_CONN())
        } yield 1
        case Some(connectionInfo) => for {
          _ <- ocesqlExecParams(connectionInfo.id, query, nParams.getOrElse(0))
        } yield 0
      }
    } yield returnCode
  }
}

class OCESQLExecParamsOccurs extends CobolRunnableWrapper {
  override def execute(args: Seq[CobolDataStorage]): Operation[Int] = {
    val query = getCString(args(1))
    val nParams = storageToInt(args(2))
    for {
      _ <- logLn("OCESQLExecParamsOccurs start")
      _ <- logLn(s"SQL:#${query.getOrElse("")}#")
      connInfo <- resolveCONNID(OCESQL_DEFAULT_DB_NAME)
      returnCode <- connInfo match {
        case None => for {
          _ <- errorLogLn("connection id is not found")
          _ <- setLibErrorStatus(OCDB_NO_CONN())
        } yield 1
        case Some(c) => for {
          _ <- ocesqlExecParamsOccurs(c.id, query, nParams.getOrElse(0))
        } yield 0
      }
    } yield returnCode
  }
}

class OCESQLIDExecParamsOccurs extends CobolRunnableWrapper {
  override def execute(args: Seq[CobolDataStorage]): Operation[Int] = {
    val atdb = storageToString(args(1), args(2))
    val query = getCString(args(3))
    val nParams = storageToInt(args(4))
    for {
      _ <- logLn("OCESQLIDExecParamsOccurs start")
      connInfo <- resolveCONNID(atdb.getOrElse(""))
      returnCode <- connInfo match {
        case None => for {
          _ <- errorLogLn("connection id is not found")
          _ <- setLibErrorStatus(OCDB_NO_CONN())
        } yield 1
        case Some(connectionInfo) => for {
          _ <- ocesqlExecParamsOccurs(connectionInfo.id, query, nParams.getOrElse(0))
        } yield 0
      }
    } yield returnCode
  }
}

class OCESQLCursorDeclare extends CobolRunnableWrapper {
  override def execute(args: Seq[CobolDataStorage]): Operation[Int] = {
    val cname = getCString(args(1))
    val query = getCString(args(2))
    for {
      _ <- logLn("OCESQLCursorDeclare start")
      _ <- logLn(s"SQL:#${query}#")
      connInfo <- resolveCONNID(OCESQL_DEFAULT_DB_NAME)
      returnCode <- connInfo match {
        case None => for {
          _ <- errorLogLn("connection id is not found")
          _ <- setLibErrorStatus(OCDB_NO_CONN())
        } yield 1
        case Some(c) => for {
          _ <- ocesqlCursorDeclare(c.id, cname, query, 0)
        } yield 0
      }
    } yield returnCode
  }
}

class OCESQLIDCursorDeclare extends CobolRunnableWrapper {
  override def execute(args: Seq[CobolDataStorage]): Operation[Int] = {
    val atdb = storageToString(args(1), args(2))
    val cname = getCString(args(3))
    val query = getCString(args(4))
    for {
      _ <- logLn("OCESQLIDCursorDeclare start")
      connInfo <- resolveCONNID(atdb.getOrElse(""))
      returnCode <- connInfo match {
        case None => for {
          _ <- errorLogLn("connection id is not found")
          _ <- setLibErrorStatus(OCDB_NO_CONN())
        } yield 1
        case Some(c) => for {
          _ <- ocesqlCursorDeclare(c.id, cname, query, 0)
        } yield 0
      }
    } yield returnCode
  }
}

class OCESQLCursorDeclareParams extends CobolRunnableWrapper {
  override def execute(args: Seq[CobolDataStorage]): Operation[Int] = {
    val cname = getCString(args(1))
    val query = getCString(args(2))
    val nParams = storageToInt(args(3))
    for {
      _ <- logLn("OCESQLCursorDeclareParams start")
      _ <- logLn(s"SQL:#${query}#")
      connInfo <- resolveCONNID(OCESQL_DEFAULT_DB_NAME)
      returnCode <- connInfo match {
        case None => for {
          _ <- errorLogLn("connection id is not found")
          _ <- setLibErrorStatus(OCDB_NO_CONN())
        } yield 1
        case Some(c) => for {
          _ <- ocesqlCursorDeclare(c.id, cname, query, nParams.getOrElse(0))
        } yield 0
      }
    } yield returnCode
  }
}

class OCESQLIDCursorDeclareParams extends CobolRunnableWrapper {
  override def execute(args: Seq[CobolDataStorage]): Operation[Int] = {
    val atdb = storageToString(args(1), args(2))
    val cname = getCString(args(3))
    val query = getCString(args(4))
    val nParams = storageToInt(args(5))
    for {
      _ <- logLn("OCESQLIDCursorDeclareParams start")
      connInfo <- resolveCONNID(atdb.getOrElse(""))
      returnCode <- connInfo match {
        case None => for {
          _ <- errorLogLn("connection id is not found")
          _ <- setLibErrorStatus(OCDB_NO_CONN())
        } yield 1
        case Some(c) => for {
          _ <- ocesqlCursorDeclare(c.id, cname, query, nParams.getOrElse(0))
        } yield 0
      }
    } yield returnCode
  }
}

class OCESQLPreparedCursorDeclare extends CobolRunnableWrapper {
  override def execute(args: Seq[CobolDataStorage]): Operation[Int] = {
    val cname = getCString(args(1))
    val sname = getCString(args(2))
    for {
      _ <- logLn("OCESQLPreparedCursorDeclare start")
      connInfo <- resolveCONNID(OCESQL_DEFAULT_DB_NAME)
      returnCode <- connInfo match {
        case None => for {
          _ <- errorLogLn("connection id is not found")
          _ <- setLibErrorStatus(OCDB_NO_CONN())
        } yield 1
        case Some(c) => for {
          _ <- ocesqlPreparedCursorDeclare(c.id, cname, sname)
        } yield 0
      }
    } yield returnCode
  }
}

class OCESQLIDPreparedCursorDeclare extends CobolRunnableWrapper {
  override def execute(args: Seq[CobolDataStorage]): Operation[Int] = {
    val atdb = storageToString(args(1), args(2))
    val cname = getCString(args(3))
    val sname = getCString(args(4))
    for {
      _ <- logLn("OCESQLIDPreparedCursorDeclare start")
      connInfo <- resolveCONNID(atdb.getOrElse(""))
      returnCode <- connInfo match {
        case None => for {
          _ <- errorLogLn("connection id is not found")
          _ <- setLibErrorStatus(OCDB_NO_CONN())
        } yield 1
        case Some(c) => for {
          _ <- ocesqlPreparedCursorDeclare(c.id, cname, sname)
        } yield 0
      }
    } yield returnCode
  }
}

class OCESQLExecPrepare extends CobolRunnableWrapper {
  override def execute(args: Seq[CobolDataStorage]): Operation[Int] = {
    val sname = getCString(args(1))
    val nParams = storageToInt(args(2))
    for {
      _ <- logLn("OCESQLExecPrepare start")
      connInfo <- resolveCONNID(OCESQL_DEFAULT_DB_NAME)
      returnCode <- connInfo match {
        case None => for {
          _ <- errorLogLn("connection id is not found")
          _ <- setLibErrorStatus(OCDB_NO_CONN())
        } yield 1
        case Some(c) => for {
          _ <- ocesqlExecPrepare(c.id, sname, nParams.getOrElse(0))
        } yield 0
      }
    } yield returnCode
  }
}

class OCESQLIDExecPrepare extends CobolRunnableWrapper {
  override def execute(args: Seq[CobolDataStorage]): Operation[Int] = {
    val atdb = storageToString(args(1), args(2))
    val sname = getCString(args(3))
    val nParams = storageToInt(args(4))
    for {
      _ <- logLn("OCESQLIDExecPrepare start")
      connInfo <- resolveCONNID(atdb.getOrElse(""))
      returnCode <- connInfo match {
        case None => for {
          _ <- errorLogLn("connection id is not found")
          _ <- setLibErrorStatus(OCDB_NO_CONN())
        } yield 1
        case Some(c) => for {
          _ <- ocesqlExecPrepare(c.id, sname, nParams.getOrElse(0))
        } yield 0
      }
    } yield returnCode
  }
}

class OCESQLCursorOpen extends CobolRunnableWrapper {
  override def execute(args: Seq[CobolDataStorage]): Operation[Int] = {
    val cname = getCString(args(1))
    val name = cname.getOrElse("")
    (for {
      _ <- operationCPure(logLn("OCESQLIDExecPrepare start"))
      _ <- operationCPure(updateState(s => s.setSqlCA(SqlCA.defaultValue)))
      _ <- operationCPure(logLn(s"cname=#${cname.getOrElse("")}#"))

      _ <- whenExecuteAndExit(name == "", for {
          _ <- setLibErrorStatus(OCDB_EMPTY())
        } yield 1)

      optionCursor <- operationCPure(getCursorFromMap(name))

      _ <- whenExecuteAndExit(optionCursor.isEmpty, for {
        _ <- errorLogLn(s"cursor ${name} not registered.")
        _ <- setLibErrorStatus(OCDB_WARNING_UNKNOWN_PORTAL())
      } yield 1)

      cursor_ <- operationCPure(operationPure(optionCursor.getOrElse(Cursor.defaultValue)))

      _ <- if (cursor_.isOpened) {
        for {
          _ <- operationCPure(logLn(s"cursor ${cname} already opened"))
          result <- operationCPure(setResultStatus(cursor_.connId))
          _ <- whenExecuteAndExit(!result, for {
            _ <- errorLogLn(s"cursor ${name} close failed")
          } yield 1)
        } yield 0
      } else {
        operationCPure[Int, Int](operationPure(0))
      }

      cursor <- operationCPure(operationPure(cursor_.setIsOpened(false)))
      _ <- operationCPure(updateCursorMap(name, cursor))

      _ <- operationCPure(if(cursor.nParams > 0) {
        //TODO implement
        /*val args = cursor.sqlVarQueue.zipWithIndex.map(s => {
          val (sv, i) = s
          createRealData(sv, i)
        })
        OCDBCursorDeclareParams(....)*/
        operationPure(())
      } else {
        cursor.sp match {
          case q :: _ =>
            OCDBCursorDeclare(cursor.connId, cursor.name, q.query, OCDB_CURSOR_WITH_HOLD_OFF)
          case _ =>
            OCDBCursorDeclare(cursor.connId, cursor.name, cursor.query, OCDB_CURSOR_WITH_HOLD_OFF)
        }
      })

      res <- operationCPure(setResultStatus(cursor.connId))
      _ <- whenExecuteAndExit(!res, operationPure(1))

      _ <- operationCPure(OCDBCursorOpen(cursor.connId, name))
      res1 <- operationCPure(setResultStatus(cursor.connId))
      _ <- whenExecuteAndExit(!res, operationPure(1))
      _ <- operationCPure(updateCursorMap(name, cursor.setIsOpened(true)))
    } yield 0).eval
  }
}

class OCESQLCursorOpenParams extends CobolRunnableWrapper {
  override def execute(args: Seq[CobolDataStorage]): Operation[Int] = {
    val cname = getCString(args(1))
    val name = cname.getOrElse("")
    val nParams = storageToInt(args(2)).getOrElse(0)
    (for {
      _ <- operationCPure(logLn("OCESQLIDExecPrepare start"))
      _ <- operationCPure(updateState(s => s.setSqlCA(SqlCA.defaultValue)))
      _ <- operationCPure(logLn(s"cname=#${cname.getOrElse("")}#"))

      _ <- whenExecuteAndExit(name == "", for {
        _ <- setLibErrorStatus(OCDB_EMPTY())
      } yield 1)

      optionCursor <- operationCPure(getCursorFromMap(name))

      _ <- whenExecuteAndExit(optionCursor.isEmpty, for {
        _ <- errorLogLn(s"cursor ${name} not registered.")
        _ <- setLibErrorStatus(OCDB_WARNING_UNKNOWN_PORTAL())
      } yield 1)

      cursor_ <- operationCPure(operationPure(optionCursor.getOrElse(Cursor.defaultValue)))

      _ <- cursor_.sp match {
        case Nil => whenExecuteAndExit(true, for {
          _ <- errorLogLn(s"prepare sql in cursor ${name} not registred.")
          _ <- setLibErrorStatus(OCDB_INVALID_STMT())
        } yield 1)
        case sp :: _ if sp.nParams != nParams => whenExecuteAndExit(true, for {
          _ <- errorLogLn(s"A number of parameters(${nParams}) and prepared sql parameters(${sp.nParams}) is unmatch")
          _ <- setLibErrorStatus(OCDB_EMPTY())
          _ <- updateState(s => {
            val errorMessage = "A number of parameters and prepared sql parameters is unmatch.".getBytes()
            val newSqlCA = s.sqlCA.setErrmc(errorMessage).setErrml(errorMessage.length.toShort)
            s.setSqlCA(newSqlCA)
          })
        } yield 1)
        case _ => operationCCPure[Int, Int](0)
      }

      _ <- if (cursor_.isOpened) {
        for {
          _ <- operationCPure(logLn(s"cursor ${cname} already opened"))
          result <- operationCPure(setResultStatus(cursor_.connId))
          _ <- whenExecuteAndExit(!result, for {
            _ <- errorLogLn(s"cursor ${name} close failed")
          } yield 1)
        } yield 0
      } else {
        operationCPure[Int, Int](operationPure(0))
      }

      cursor <- operationCPure(operationPure(cursor_.setIsOpened(false)))
      _ <- operationCPure(updateCursorMap(name, cursor))

      //TODO implement

      res <- operationCPure(setResultStatus(cursor.connId))
      _ <- whenExecuteAndExit(!res, operationPure(1))

      _ <- operationCPure(OCDBCursorOpen(cursor.connId, name))
      res1 <- operationCPure(setResultStatus(cursor.connId))
      _ <- whenExecuteAndExit(!res, operationPure(1))
      _ <- operationCPure(updateCursorMap(name, cursor.setIsOpened(true)))
    } yield 0).eval
  }
}

class OCESQLCursorFetchOne extends CobolRunnableWrapper {
  override def execute(args: Seq[CobolDataStorage]): Operation[Int] = {
    val cname = getCString(args(1))
    val name = cname.getOrElse("")
    (for {
      _ <- operationCPure(initSqlca())
      _ <- whenExecuteAndExit(name.length == 0,
        setLibErrorStatus(OCDB_EMPTY()).flatMap(_ => operationPure(1)))

      _ <- operationCPure(logLn(s"cname:${name}"))
      returnCode <- operationCPure(getCursorFromMap(name).flatMap(_ match {
        case None => for {
          _ <- errorLogLn(s"cursor ${name} not registered.")
          _ <- setLibErrorStatus(OCDB_WARNING_UNKNOWN_PORTAL())
        } yield 1
        case Some(cursor) => {
          val id = cursor.connId
          (for {
            state <- operationCPure(getState)
            _ <- operationCPure(OCDBCursorFetchOne(id, name, OCDB_READ_NEXT()))
            resultStatus <- operationCPure(setResultStatus(id))
            _ <- whenExecuteAndExit(!resultStatus, operationPure(1))

            fields <- operationCPure(OCDBNfields(id))

            _ <- whenExecuteAndExit(fields != state.globalState.sqlResVarQueue.length, for {
              _ <- errorLogLn(s"A number of parameters ${state.globalState.sqlResVarQueue.length} " +
                s"and results(${fields}) is unmatch.")
              _ <- setLibErrorStatus(OCDB_EMPTY())
              _ <- updateState(s => {
                val sqlCA = s.sqlCA
                val errorMessage = s"A number of Parameters and results is unmatch.".getBytes()
                s.setSqlCA(sqlCA.setErrmc(errorMessage).setErrml(errorMessage.length.toShort))
              })
            } yield 1)

            _ <- operationCPure(lookUpConnList(id).flatMap(_ match {
              case None => operationPure(())
              case Some((_, conn)) => conn.result match {
                case Right(EResultSet(rs)) => if(rs.next()) {
                  forM(state.globalState.sqlResVarQueue.zipWithIndex)(e => {
                    val (sv, i) = e
                    if(i >= fields){
                      operationPure(())
                    } else {
                      for {
                        retStr <- OCDBGetValue(id, i + 1)
                        _ <- retStr match {
                          case Some(str) =>
                            operationPure(createCobolData(sv, 0, str, state.globalState.occursInfo))
                          case _ =>
                            operationPure(())
                        }
                      } yield ()
                    }
                  }).flatMap(_ => operationPure(()))
                } else {
                  setLibErrorStatus(OCDB_NOT_FOUND()).map(_ => ())
                }
                case _ => setLibErrorStatus(OCDB_NOT_FOUND()).map(_ => ())
              }
            }))

            newTuples <- operationCCPure(cursor.tuples + state.sqlCA.errd(2))
            _ <- operationCPure(updateCursorMap(name, cursor.setTuples(newTuples)))
            _ <- operationCPure(updateState(s => {
              var sqlCA = s.sqlCA
              sqlCA.errd(2) = newTuples
              s.setSqlCA(sqlCA)
            }))

          } yield 0).eval
        }
      }))
    } yield returnCode).eval
  }
}

class OCESQLCursorFetchOccurs extends CobolRunnableWrapper {
  override def execute(args: Seq[CobolDataStorage]): Operation[Int] = {
    val cname = getCString(args(1))
    val name = cname.getOrElse("")
    (for {
      _ <- operationCPure(initSqlca())
      _ <- whenExecuteAndExit(name.length == 0,
        setLibErrorStatus(OCDB_EMPTY()).flatMap(_ => operationPure(1)))

      _ <- operationCPure(logLn(s"cname:${name}"))
      returnCode <- operationCPure(getCursorFromMap(name).flatMap(_ match {
        case None => for {
          _ <- errorLogLn(s"cursor ${name} not registered.")
          _ <- setLibErrorStatus(OCDB_WARNING_UNKNOWN_PORTAL())
        } yield 1
        case Some(cursor) => {
          val id = cursor.connId
          (for {
            state <- operationCPure(getState)
            _ <- operationCPure(OCDBCursorFetchOccurs(id, name, OCDB_READ_NEXT(), state.globalState.occursInfo.length))
            newState <- operationCPure(getState)
            _ <- whenExecuteAndExit(newState.sqlCA.code < 0, operationPure(1))
            fields <- operationCPure(OCDBNfields(id))

            _ <- whenExecuteAndExit(fields != newState.globalState.sqlResVarQueue.length, for {
              _ <- errorLogLn(s"A number of parameters ${newState.globalState.sqlResVarQueue.length} " +
               s"and results(${fields} is unmatch.")
              _ <- setLibErrorStatus(OCDB_EMPTY())
              _ <- updateState(s => {
                val sqlCA = s.sqlCA
                val errorMessage = s"A number of Parameters and results is unmatch.".getBytes()
                s.setSqlCA(sqlCA.setErrmc(errorMessage).setErrml(errorMessage.length.toShort))
              })
            } yield 1)

            _ <- operationCPure(lookUpConnList(id).flatMap(_ match {
              case None => operationPure()
              case Some((_, conn)) => conn.result match {
                case Right(EResultSet(rs)) => for {
                  tuples <- resultSetToSqlVar(rs, id, 0, 0, newState.globalState.sqlResVarQueue, newState.globalState.occursInfo)
                  _ <- updateCursorMap(name, cursor.setTuples(tuples))
                } yield ()
                case _ => operationPure(())
              }
            }))

            _ <- operationCPure(updateState(s => {
              var sqlCA = s.sqlCA
              sqlCA.errd(2) = cursor.tuples
              s.setSqlCA(sqlCA)
            }))

          } yield 0).eval
        }
      }))
    } yield returnCode).eval
  }
}

class OCESQLCursorClose extends CobolRunnableWrapper {
  override def execute(args: Seq[CobolDataStorage]): Operation[Int] = {
    val cname = getCString(args(1))
    val name = cname.getOrElse("")
    for {
      _ <- initSqlca()
      returnCode <- if(name == "") {
        setLibErrorStatus(OCDB_EMPTY()).flatMap(_ => operationPure(1))
      } else {
        for {
          _ <- logLn(s"Cursor Name: ${name}")
          returnCode <- getCursorFromMap(name).flatMap(_ match {
            case None => for {
              _ <- errorLog(s"cursor ${name} not registered")
              _ <- setLibErrorStatus(OCDB_WARNING_UNKNOWN_PORTAL())
            } yield 1
            case Some(cursor) if !cursor.isOpened =>
              logLn(s"cursor ${name} not opened.").flatMap(_ => operationPure(0))
            case Some(cursor) => for {
              _ <- logLn(s"Connect ID: ${cursor.connId}")
              _ <- OCDBCursorClose(cursor.connId, name)
              res <- setResultStatus(cursor.connId)
              returnCode <- if(res) {
                for{
                  _ <- updateCursorMap(name, cursor.setIsOpened(false))
                } yield 0
              } else {
                operationPure(1)
              }
            } yield returnCode
          })
        } yield returnCode
      }
    } yield returnCode
  }
}


class OCESQLExecSelectIntoOne extends CobolRunnableWrapper {
  override def execute(args: Seq[CobolDataStorage]): Operation[Int] = {
    val query = getCString(args(1))
    val nParams = storageToInt(args(2))
    val nResParams = storageToInt(args(3))
    for {
      _ <- logLn("OCESQLExecSelectIntoOne start")
      _ <- logLn(s"SQL:#${query}#")
      connInfo <- resolveCONNID(OCESQL_DEFAULT_DB_NAME)
      returnCode <- connInfo match {
        case None => for {
          _ <- errorLogLn("connection id is not found")
          _ <- setLibErrorStatus(OCDB_NO_CONN())
        } yield 1
        case Some(connectionInfo) => for {
          _ <- ocesqlExecSelectIntoOne(connectionInfo.id, query, nParams.getOrElse(0), nResParams.getOrElse(0))
        } yield 0
      }
    } yield returnCode
  }
}

class OCESQLIDExecSelectIntoOne extends CobolRunnableWrapper {
  override def execute(args: Seq[CobolDataStorage]): Operation[Int] = {
    val atdb = storageToString(args(1), args(2))
    val query = getCString(args(3))
    val nParams = storageToInt(args(4))
    val nResParams = storageToInt(args(5))
    for {
      _ <- logLn("OCESQLIDExecSelectIntoOne start")
      connInfo <- resolveCONNID(atdb.getOrElse(""))
      returnCode <- connInfo match {
        case None => for {
          _ <- errorLogLn("connection id is not found")
          _ <- setLibErrorStatus(OCDB_NO_CONN())
        } yield 1
        case Some(connectionInfo) => for {
          _ <- ocesqlExecSelectIntoOne(connectionInfo.id, query, nParams.getOrElse(0), nResParams.getOrElse(0))
        } yield 0
      }
    } yield returnCode
  }
}

class OCESQLExecSelectIntoOccurs extends CobolRunnableWrapper {
  override def execute(args: Seq[CobolDataStorage]): Operation[Int] = {
    val query = getCString(args(1))
    val nParams = storageToInt(args(2))
    val nResParams = storageToInt(args(3))
    for {
      _ <- logLn("OCESQLExecSelectIntoOccurs start")
      connInfo <- resolveCONNID(OCESQL_DEFAULT_DB_NAME)
      returnCode <- connInfo match {
        case None => for {
          _ <- errorLogLn("connection id is not found")
          _ <- setLibErrorStatus(OCDB_NO_CONN())
        } yield 1
        case Some(connectionInfo) => for {
          _ <- ocesqlExecSelectIntoOccurs(connectionInfo.id, query, nParams.getOrElse(0), nResParams.getOrElse(0))
        } yield 0
      }
    } yield returnCode
  }
}

class OCESQLIDExecSelectIntoOccurs extends CobolRunnableWrapper {
  override def execute(args: Seq[CobolDataStorage]): Operation[Int] = {
    val atdb = storageToString(args(1), args(2))
    val query = getCString(args(3))
    val nParams = storageToInt(args(4))
    val nResParams = storageToInt(args(5))
    for {
      _ <- logLn("OCESQLIDExecSelectIntoOccurs start")
      connInfo <- resolveCONNID(atdb.getOrElse(""))
      returnCode <- connInfo match {
        case None => for {
          _ <- errorLogLn("connection id is not found")
          _ <- setLibErrorStatus(OCDB_NO_CONN())
        } yield 1
        case Some(connectionInfo) => for {
          _ <- ocesqlExecSelectIntoOccurs(connectionInfo.id, query, nParams.getOrElse(0), nResParams.getOrElse(0))
        } yield 0
      }
    } yield returnCode
  }
}

class OCESQLStartSQL extends CobolRunnableWrapper {
  parseSqlCA = false
  override def execute(args: Seq[CobolDataStorage]): Operation[Int] = for {
    _ <- logLn("#begin")
    _ <- initSqlVarQueue()
    _ <- logLn("#end")
  } yield 0
}

class OCESQLSetSQLParams extends CobolRunnableWrapper {
  parseSqlCA = false
  override def execute(args: Seq[CobolDataStorage]): Operation[Int] = {
    val paramType = storageToInt(args(0)).getOrElse(0)
    val paramLength = storageToInt(args(1)).getOrElse(0)
    val scale = storageToInt(args(2)).getOrElse(0)
    val storage = args(3)
    val oStorage = Option(storage)

    (for {
      _ <- whenExecuteAndExit(paramType < OCDB_TYPE_MIN || paramType > OCDB_TYPE_MAX, for {
        _ <- errorLogLn(s"invalid argument 'type': ${paramType}")
      } yield 1)
      _ <- whenExecuteAndExit(paramLength < 0, for {
        _ <- errorLogLn(s"invalid argument 'length': ${paramLength}")
      } yield 1)
      _ <- whenExecuteAndExit(oStorage.isEmpty, for {
        _ <- errorLogLn(s"finvalid argument addr is NULL").map(_ => 1)
      } yield 1)
      _ <- operationCPure(addSqlVarQueue(paramType, paramLength, scale, oStorage))
    } yield 0).eval
  }
}

class OCESQLSetResultParams extends CobolRunnableWrapper {
  parseSqlCA = false
  override def execute(args: Seq[CobolDataStorage]): Operation[Int] = {
    val paramType = storageToInt(args(0)).getOrElse(0)
    val paramLength = storageToInt(args(1)).getOrElse(0)
    val scale = storageToInt(args(2)).getOrElse(0)
    val storage = args(3)
    val oStorage = Option(storage)

    (for {
      _ <- whenExecuteAndExit(paramType < OCDB_TYPE_MIN || paramType > OCDB_TYPE_MAX, for {
        _ <- errorLogLn(s"invalid argument 'type': ${paramType}")
        } yield 1)
      _ <- whenExecuteAndExit(paramLength < 0, for {
        _ <- errorLogLn(s"invalid argument 'length': ${paramLength}")
        } yield 1)
      _ <- whenExecuteAndExit(oStorage.isEmpty, for {
        _ <- errorLogLn(s"finvalid argument addr is NULL")
        } yield 1)
      _ <- operationCPure(addSqlResVarQueue(paramType, paramLength, scale, oStorage))
    } yield 0).eval
  }
}

class OCESQLSetHostTable extends CobolRunnableWrapper {
  parseSqlCA = false
  override def execute(args: Seq[CobolDataStorage]): Operation[Int] = {
    val iter = storageToInt(args(0)).getOrElse(0)
    val length = storageToInt(args(1)).getOrElse(0)
    val isParent = storageToInt(args(2)).getOrElse(0) != 0

    if(iter < 0) {
      for {
        _ <- errorLogLn(s"invalid argument 'iter': ${iter}")
      } yield 1
    } else if(length < 0) {
      for {
        _ <- errorLogLn(s"invalid argument 'length': ${length}")
      } yield 1
    } else {
      for {
        _ <- updateState(state => {
          val globalState = state.globalState
          val newGlobalState = globalState.setOccursInfo(new OccursInfo(iter, length, isParent))
          state.setGlobalState(newGlobalState)
        })
      } yield 0
    }
  }
}

class OCESQLEndSQL extends CobolRunnableWrapper {
  parseSqlCA = false

  override def execute(args: Seq[CobolDataStorage]): Operation[Int] = for {
    state <- getState
    _ <- logLn("#debug start dump var_list")
    _ <- showSqlVarQueue(state.globalState.sqlVarQueue)
    _ <- logLn("#debug start dump res_list")
    _ <- showSqlVarQueue(state.globalState.sqlResVarQueue)

    _ <- resetSqlVarQueue()
  } yield 0
}