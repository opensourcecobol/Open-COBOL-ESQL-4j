import Operation._
import Common._
import ConstValues._
import scala.collection.immutable.Queue
import java.sql.Connection

class Cursor(
            val connId : Int,
            val name : String,
            val sp: List[QueryInfo],
            val query: String,
            val nParams: Int,
            val isOpened: Boolean,
            val tuples: Int,
            val sqlVarQueue: Queue[SQLVar], //pList
            ) {
  def setConnId(connId: Int): Cursor = new Cursor(connId, name, sp, query, nParams, isOpened, tuples, sqlVarQueue)
  def setName(name: String): Cursor = new Cursor(connId, name, sp, query, nParams, isOpened, tuples, sqlVarQueue)
  def setSp(sp: List[QueryInfo]): Cursor = new Cursor(connId, name, sp, query, nParams, isOpened, tuples, sqlVarQueue)
  def setQuery(query: String): Cursor = new Cursor(connId, name, sp, query, nParams, isOpened, tuples, sqlVarQueue)
  def setNParams(nParams: Int): Cursor = new Cursor(connId, name, sp, query, nParams, isOpened, tuples, sqlVarQueue)
  def setIsOpened(isOpened: Boolean): Cursor = new Cursor(connId, name, sp, query, nParams, isOpened, tuples, sqlVarQueue)
  def setTuples(tuples: Int): Cursor = new Cursor(connId, name, sp, query, nParams, isOpened, tuples, sqlVarQueue)
  def setSqlVarQueue(sqlVarQueue: Queue[SQLVar]): Cursor = new Cursor(connId, name, sp, query, nParams, isOpened, tuples, sqlVarQueue)
}

object Cursor {
  type CursorMap = scala.collection.immutable.Map[String, Cursor]
  type QueryInfoMap = scala.collection.immutable.Map[String, QueryInfo]
  def defaultValue(): Cursor = new Cursor(0, "", Nil, "", 0, false, 0, Queue())
  def emptyCursorMap: CursorMap = Map.empty

  def clearCursorMap(id: Int): Operation[Unit] = for {
    state <- getState
    _ <- setState({
      val globalState = state.globalState
      val cursorMap = globalState.cursorMap
      val newCursorMap = cursorMap.transform((_, cursor) =>
        if(cursor.connId == id) {
          cursor.setIsOpened(false)
        } else {
          cursor
        })
      val newGlobalState = globalState.setCursorMap(newCursorMap)
      state.setGlobalState(newGlobalState)
    })
  } yield ()

  def getCursorFromMap(cname: String): Operation[Option[Cursor]] = for {
    _ <- logLn(s"target:${cname}")
    state <- getState
    cursor <- state.globalState.cursorMap.get(cname) match {
      case None => for {
          _ <- errorLogLn(s"cursor name '${cname}' is not found in cursor list.")
        } yield None
      case Some(c) => for {
        _ <- logLn(s"#return:${c.name}#")
      } yield Some(c)
    }
  } yield cursor

  def updateCursorMap(cname: String, cursor: Cursor): Operation[Unit] =
    updateState(s => {
      val newCursorMap = s.globalState.cursorMap + (cname -> cursor)
      val newGlobalState = s.globalState.setCursorMap(newCursorMap)
      s.setGlobalState(newGlobalState)
    })

  def addCursorMap(id: Int, cname: String, query: String, nParams: Int): Operation[Boolean] = {

    val registerCursor = for {
      _ <- updateState(s => {
        val globalState = s.globalState
        val sqlVarQueue = globalState.sqlVarQueue
        val newCursor = Cursor.defaultValue()
          .setName(cname)
          .setConnId(id)
          .setQuery(query)
          .setNParams(nParams)
          .setSqlVarQueue(if(nParams > 0) {sqlVarQueue} else {Queue()})
          .setIsOpened(false)
          .setTuples(0)
        val newCursorMap = globalState.cursorMap + (cname -> newCursor)
        val newGlobalState = globalState.setCursorMap(newCursorMap)
        s.setGlobalState(newGlobalState)
      })
    } yield true

    for {
      state <- getState
      cursorMap <- operationPure(state.globalState.cursorMap)
      ret <- cursorMap.get(cname) match {
        case Some(cursor) if (cursor.isOpened)  => for {
            _ <- errorLogLn(s"cursor name '${cname}' alrready registred and opend").flatMap(_ => operationPure(RESULT_FAILED))
          } yield false
        case _ => registerCursor
      }
    } yield ret
  }

  def addCursorMapWithPrepare(id: Int, cname: String, prepare: QueryInfo): Operation[Boolean] = {
    val registerCursor = for {
      _ <- updateState(s => {
        val globalState = s.globalState
        val newCursor = Cursor.defaultValue()
          .setName(cname)
          .setConnId(id)
          .setSp(List(prepare))
          .setIsOpened(false)
          .setTuples(0)
        val newCursorMap = globalState.cursorMap + (cname -> newCursor)
        val newGlobalState = globalState.setCursorMap(newCursorMap)
        s.setGlobalState(newGlobalState)
      })
    } yield true

    for {
      state <- getState
      cursorMap <- operationPure(state.globalState.cursorMap)
      ret <- cursorMap.get(cname) match {
        case Some(cursor) if (cursor.isOpened)  => for {
          _ <- errorLogLn(s"cursor name '${cname}' alrready registred and opend").flatMap(_ => operationPure(RESULT_FAILED))
        } yield false
        case _ => registerCursor
      }
    } yield ret
  }

  def ocesqlCursorDeclare(id: Int, cname: Option[String], query: Option[String], nParams: Int): Operation[Unit] = {
    val cname_ = cname.getOrElse("")
    val query_ = query.getOrElse("")
    for {
      state <- getState
      _ <- setState(state.setSqlCA(SqlCA.defaultValue))
      _ <- if (cname_ == "" || query_ == "") {
        setLibErrorStatus(OCDB_EMPTY()).flatMap(_ => operationPure(()))
      } else {
        for {
          res <- addCursorMap(id, cname_, query_, nParams)
          _ <- if(res) {
              operationPure(0)
            } else {
              setLibErrorStatus(OCDB_WARNING_PORTAL_EXISTS())
            }
        } yield ()
      }
    } yield ()
  }

  def ocesqlPreparedCursorDeclare(id: Int, cname: Option[String], sname: Option[String]): Operation[Unit] = {
    val cname_ = cname.getOrElse("")
    val sname_ = sname.getOrElse("")
    for {
      state <- getState
      _ <- setState(state.setSqlCA(SqlCA.defaultValue))
      _ <- if (cname_ == "" || sname_ == "") {
        setLibErrorStatus(OCDB_EMPTY()).flatMap(_ => operationPure(()))
      } else {
        for {
          _ <- getPrepareFromMap(sname_).flatMap(_ match {
            case None => for {
                _ <- errorLogLn(s"prepare ${sname_} not registered.")
                _ <- setLibErrorStatus(OCDB_INVALID_STMT())
              } yield ()
            case Some(prepare) => for {
              res <- addCursorMapWithPrepare(id, cname_, prepare)
              _ <- if(res) {
                  setLibErrorStatus(OCDB_WARNING_PORTAL_EXISTS())
                } else {
                  operationPure(0)
                }
              } yield ()
          })
        } yield ()
      }
    } yield ()
  }

  def getPrepareFromMap(sname: String): Operation[Option[QueryInfo]] = for {
    state <- getState
    queryInfoMap <- operationPure(state.globalState.queryInfoMap)
    retValue <- queryInfoMap.get(sname) match {
      case None => for {
        _ <- errorLogLn(s"prepare name '${sname}' is not found in prepare list.")
        _ <- showQueryInfoMap()
      } yield None
      case Some(queryInfo) => for {
        _ <- logLn(s"#return:${queryInfo.pName}#")
      } yield Some(queryInfo)
    }
  } yield retValue

  // TODO implement
  private def showQueryInfoMap(): Operation[Unit] = operationPure(())

  def ocesqlExecPrepare(id: Int, sname: Option[String], nParams: Int): Operation[Unit] ={
    // TODO implement
    operationPure(())
  }

  def OCDBCursorDeclare(id: Int, cname: String, query: String, withHold: Boolean): Operation[Unit] =
    lookUpConnList(id).flatMap(_ match {
      case None => operationPure(())
      case Some((_, pConn)) => for {
        _ <- OCDB_PGExec(pConn.connAddr, "BEGIN")
        result <- OCDB_PGCursorDeclear(pConn.connAddr, cname, query, withHold)
        _ <- updateConnList(id, pConn.setResult(result).setResult(Right(ESuccess())))
        _ <- when(result.isLeft, errorLogLn("PostgreSQL Result is NULL"))
      } yield ()
    })

  def OCDB_PGCursorDeclear(conn: Option[Connection], cname: String, query: String, withHold: Boolean): Operation[ExecResult] = {
    val command = if(withHold == OCDB_CURSOR_WITH_HOLD_ON) {
      s"DECLARE ${cname} CURSOR WITH HOLD FOR ${query}"
    } else {
      s"DECLARE ${cname} CURSOR FOR ${query}"
    }
    for{
      result <- OCDB_PGExec(conn, command)
      _ <- result match {
        case Left(e) =>
          operationPure(e.printStackTrace())
        case _ =>
          logLn("declare cursor success")
        }
    } yield result
  }

  def OCDBCursorOpen(id: Int, cname: String): Operation[Unit] =
    lookUpConnList(id).flatMap(_ match {
      case None =>
        operationPure(())
      case Some((_, pConn)) => for {
        _ <- updateConnList(id, pConn.setResult(Right(ESuccess())))
      } yield ()
    })

  def OCDBCursorFetchOccurs(id: Int, cname: String, fetchMode: ReadDirection, count: Int): Operation[Unit] =
    lookUpConnList(id).flatMap(_ match {
      case None =>
        operationPure(())
      case Some((_, pConn)) => for {
        _ <- logLn(s"addr:${pConn.connAddr.getOrElse("")}, cname:${cname}, mode:${fetchMode}, count:${count}")
        _ <- OCDB_PGCursorFetchOccurs(pConn, cname, fetchMode, count)
      } yield ()
    })

  def OCDB_PGCursorFetchOccurs(conn: ConnectionInfo, cname: String, fetchMode: ReadDirection, count: Int): Operation[Unit] = {
    val strReadMode = fetchMode match {
      case OCDB_READ_PREVIOUS() => "BACKWARD"
      case _ => "FORWARD"
    }
    OCDBExec(conn.id,s"FETCH ${strReadMode} ${count} FROM ${cname}")
  }

  def OCDBCursorFetchOne(id: Int, cname: String, fetchMode: ReadDirection): Operation[Unit] =
    lookUpConnList(id).flatMap(_ match {
      case None =>
        operationPure(())
      case Some((_, pConn)) => for {
        _ <- logLn(s"addr:${pConn.connAddr.getOrElse("")}, cname:${cname}, mode:${fetchMode}")
        _ <- OCDB_PGCursorFetchOne(pConn, cname, fetchMode)
      } yield ()
    })

  def OCDB_PGCursorFetchOne(conn: ConnectionInfo, cname: String, fetchMode: ReadDirection): Operation[Unit] = {
    val direction = fetchMode match {
      case OCDB_READ_PREVIOUS() => -1
      case OCDB_READ_CURRENT() => 0
      case OCDB_READ_NEXT() => 1
    }
    OCDBExec(conn.id,s"FETCH RELATIVE ${direction} FROM ${cname}")
  }

  def OCDBCursorClose(id: Int, cname: String): Operation[Unit] =
    lookUpConnList(id).flatMap(_ match {
      case None =>
        operationPure(())
      case Some((_, pConn)) => for {
        res <- OCDB_PGCursorClose(pConn.connAddr, cname)
        _ <- OCDB_PGExec(pConn.connAddr, "COMMIT")
        _ <- updateConnList(id, pConn.setResult(res))
      } yield ()
    })

  def OCDB_PGCursorClose(conn: Option[Connection], cname: String): Operation[ExecResult] =
    OCDB_PGExec(conn, s"CLOSE ${cname}")
}