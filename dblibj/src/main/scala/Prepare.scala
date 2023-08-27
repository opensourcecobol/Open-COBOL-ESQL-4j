import Operation._
import ConstValues.DATA_SIZE_OF_SQL_COMMAND_LEN
import jp.osscons.opensourcecobol.libcobj.data.CobolDataStorage
import java.nio.charset.StandardCharsets

class Prepare {

}

object Prepare {
  def getStrReplaceHostValue(target: String): (Int, String) = {
    val regexHostVariable = ":[^, \\)]+".r
    val matches = regexHostVariable.findAllIn(target)
    val count = matches.length
    val replaced = regexHostVariable.replaceAllIn(target, "?")
    (count, replaced)
  }

  def addQueryInfoMap(name: String, query: String, nParams: Int, state: OCDBState): Unit = {
    val newQueryInfoMap = state.globalState.queryInfoMap + (name -> new QueryInfo(name, query, nParams))
    val newGlobalState = state.globalState.setQueryInfoMap(newQueryInfoMap)
    state.updateGlobalState(newGlobalState)
  }

  def parsePrepareQuery(stringDataStorage: CobolDataStorage, lengthDataStorage: CobolDataStorage): String = {
    var length: Int = 0
    for(i <- 0 until DATA_SIZE_OF_SQL_COMMAND_LEN) {
      length = length * 10 + lengthDataStorage.getByte(i) - '0'.toByte
    }

    new String(stringDataStorage.getByteArray(0, length), "SHIFT-JIS")
  }
}