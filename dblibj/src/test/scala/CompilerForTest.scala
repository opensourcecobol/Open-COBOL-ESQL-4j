import cats.data.State
import Operation._
import cats.~>

object CompilerForTest {
  class TestEnv(val log: Seq[String], val envValues: Map[String, String])

  type Env[A] = State[TestEnv, A]
  def getCompiler: CommandA ~> Env = new (CommandA ~> Env) {
    def apply[A](fa: CommandA[A]): Env[A] =
      fa match {
        case Connect(dbname, host, port, user, passwd, encoding) =>
          for {
            _ <- State.modify((e: TestEnv) =>
              new TestEnv(e.log ++ Seq(s"[connect]: dbname: ${dbname}, host: ${host}, port: ${port}, user: ${user}, passwd: ${passwd}, encoding: ${encoding}"), e.envValues))
          } yield None
        case Exec(addr, query) => for {
          _ <- State.modify((e: TestEnv) =>
            new TestEnv(e.log ++ Seq(s"[exec]: addr: ${addr} query: ${query}"), e.envValues))
        } yield None
        case GetEnvValue(key) => State.inspect(_.envValues.get(key))
        case Log(msg) => State.modify((e: TestEnv) =>
          new TestEnv(e.log ++ Seq(s"log: ${msg}"), e.envValues))
        case ErrorLog(msg) => State.modify((e: TestEnv) =>
          new TestEnv(e.log ++ Seq(s"errorlog: ${msg}"), e.envValues))
      }
  }
}
