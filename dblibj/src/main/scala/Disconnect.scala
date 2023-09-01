import Common._

object OCESQLDisconnectCore {
  def disconnect(id: Int, state: OCDBState): Unit = {
    ocdbExec(id, "COMMIT", state)
    ocdbFinish(id, state)
  }
}
