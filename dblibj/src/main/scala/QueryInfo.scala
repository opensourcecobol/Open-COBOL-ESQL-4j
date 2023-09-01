class QueryInfo(val pName: String, val query: String, val nParams: Int) {}

object QueryInfo {
  def defaultValue: QueryInfo = new QueryInfo("", "", 0)
}
