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

object GlobalState {
  var globalState: GlobalState = initialGlobalState
  def initialGlobalState: GlobalState = new GlobalState(ConnectionInfo.emptyConnectionMap, false, Cursor.emptyCursorMap, Queue.empty, Queue.empty, Map.empty, OccursInfo.defaultValue)
}
