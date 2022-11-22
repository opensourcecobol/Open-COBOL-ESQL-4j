
import jp.osscons.opensourcecobol.libcobj.data.CobolDataStorage

class OCDBState(val sqlCA: SqlCA, val globalState: GlobalState) {
  def setSqlCA(sqlCA: SqlCA): OCDBState = new OCDBState(sqlCA, this.globalState)
  def setGlobalState(globalState: GlobalState): OCDBState = new OCDBState(this.sqlCA, globalState)
}

object OCDBState {
  def initialState(storage: CobolDataStorage): OCDBState = new OCDBState(SqlCA.storageToSqlCA(storage), GlobalState.globalState)
  def updateByState(storage: CobolDataStorage, state: OCDBState): Unit = {
    state.sqlCA.set(storage)
    updateByState(state)
  }

  def updateByState(state: OCDBState): Unit = {
    GlobalState.globalState = state.globalState
  }

  def defaultValue: OCDBState = new OCDBState(SqlCA.defaultValue, GlobalState.initialGlobalState)
}
