class OccursInfo(val iter: Int, val length: Int, val isPresent: Boolean) {
  def setLength(length: Int): OccursInfo =
    new OccursInfo(length, iter, isPresent)
  def setIter(iter: Int): OccursInfo = new OccursInfo(length, iter, isPresent)
  def setIsPresent(isPresent: Boolean): OccursInfo =
    new OccursInfo(length, iter, isPresent)
}

object OccursInfo {
  def defaultValue = new OccursInfo(0, 0, false)
}
