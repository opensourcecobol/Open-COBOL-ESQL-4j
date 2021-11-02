import org.scalatest.FunSuite
import CompilerForTest._
import Operation._

class ConnectTest extends FunSuite {
  test("Connect Default") {
    val program: Operation[Int] = OCESQLConnectCore.connect(None, None, None, None)
    val envValues: Map[String, String] = Map.empty
    val testEnv = new TestEnv(Seq(), envValues)
    val (newState, result) = program.run(emptyOCBDState).foldMap(CompilerForTest.getCompiler).run(testEnv).value
    println(newState.log)
    val expectedLog: Seq[String] = Seq(
      ln("log: param OCDB_DB_NAME is not set. set default value. "),
      ln("log: param OCDB_DB_USER is not set. set default value. "),
      ln("log: param OCDB_DB_PASS is not set. set default value. "),
      ln("log: param OCDB_DB_CHAR is not set. set default value. "),
      ln("log: dbname   = "),
      ln("log: user     = "),
      ln("log: password = "),
      ln("log: connname = OCDB_DEFAULT_DBNAME"),
    )
    //assert(newState.log == expectedLog)
  }

  def ln(s: String): String = s + System.lineSeparator()
  def emptyOCBDState: OCDBState = new OCDBState(SqlCA.defaultValue, GlobalState.initialGlobalState)
}