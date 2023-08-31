import Common._

object OCESQLDisconnectCore {
  def disconnect(id: Int, state: OCDBState): Unit = {
    OCDBExec(id, "COMMIT", state)
    OCDB_Finish(id, state)
  }
}
