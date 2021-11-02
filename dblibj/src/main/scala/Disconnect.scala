import Operation._
import Common._

object OCESQLDisconnectCore {
  def disconnect(id: Int): Operation[Unit] =
    OCDB_Finish(id)
}
