import ConnectionInfo._
import Cursor.CursorMap
import Cursor.QueryInfoMap
import scala.collection.immutable.Queue

class GlobalState(val connectionMap: ConnectionMap,
                  val rollbackOneMode: Boolean,
                  val cursorMap: CursorMap,
                  val sqlVarQueue: Queue[SQLVar],
                  val sqlResVarQueue: Queue[SQLVar], //prepareList
                  val queryInfoMap: QueryInfoMap,
                  val occursInfo: OccursInfo) {
  def setConnectionMap(connectionMap: ConnectionMap): GlobalState = new GlobalState(connectionMap, rollbackOneMode, cursorMap, sqlVarQueue, sqlResVarQueue, queryInfoMap, occursInfo)
  def setRollBackOneMode(rollbackOneMode: Boolean): GlobalState = new GlobalState(connectionMap, rollbackOneMode, cursorMap, sqlVarQueue, sqlResVarQueue, queryInfoMap, occursInfo)
  def setCursorMap(cursorMap: CursorMap): GlobalState = new GlobalState(connectionMap, rollbackOneMode, cursorMap, sqlVarQueue, sqlResVarQueue, queryInfoMap, occursInfo)
  def setSqlVarQueue(sqlVarQueue: Queue[SQLVar]): GlobalState = new GlobalState(connectionMap, rollbackOneMode, cursorMap, sqlVarQueue, sqlResVarQueue, queryInfoMap, occursInfo)
  def setSqlResVarQueue(sqlResVarQueue: Queue[SQLVar]): GlobalState = new GlobalState(connectionMap, rollbackOneMode, cursorMap, sqlVarQueue, sqlResVarQueue, queryInfoMap, occursInfo)
  def setQueryInfoMap(queryInfoMap: QueryInfoMap): GlobalState = new GlobalState(connectionMap, rollbackOneMode, cursorMap, sqlVarQueue, sqlResVarQueue, queryInfoMap, occursInfo)
  def setOccursInfo(occursInfo: OccursInfo): GlobalState = new GlobalState(connectionMap, rollbackOneMode, cursorMap, sqlVarQueue, sqlResVarQueue, queryInfoMap, occursInfo)
}

sealed trait PIC_N_Charset
case class PIC_N_SJIS() extends PIC_N_Charset
case class PIC_N_UTF16BE() extends PIC_N_Charset

object GlobalState {
  var globalState: GlobalState = initialGlobalState

  var fetch_records: Int = 1
  val SWITCH_PIC_N_ENV_VAR_NAME = "OCESQL4J_UTF16BE"
  val FETCH_RECORDS_ENV_VAR_NAME = "OCESQL4J_FETCH_RECORDS"

  def getFetchRecords = fetch_records
  def setFetchRecords(x: Int) = {
    if(x > 0) {
      fetch_records = x
    }
  }

  def initialGlobalState: GlobalState = new GlobalState(ConnectionInfo.emptyConnectionMap, false, Cursor.emptyCursorMap, Queue.empty, Queue.empty, Map.empty, OccursInfo.defaultValue)
}
