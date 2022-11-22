import org.scalatest.FunSuite
import Prepare._

class ConnectTest extends FunSuite {
  test("Connect Default") {
    assert(getStrReplaceHostValue("SQL :hello, :world_") == (2, "SQL ?, ?"))
    assert(getStrReplaceHostValue("SQL :hello, :world_ :WORLD-WORLD") == (3, "SQL ?, ? ?"))
  }

  def ln(s: String): String = s + System.lineSeparator()
  def emptyOCBDState: OCDBState = new OCDBState(SqlCA.defaultValue, GlobalState.initialGlobalState)
}