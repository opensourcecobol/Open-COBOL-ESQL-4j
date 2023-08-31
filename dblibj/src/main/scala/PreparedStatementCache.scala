import java.sql.{PreparedStatement, ParameterMetaData}
import scala.collection.mutable.HashMap

object PreparedStatementCache {
  private var statementCache: HashMap[String, ParameterMetaData] = HashMap.empty
  def getParameterMetaDataFromCache(
      sql: String,
      stmt: PreparedStatement
  ): ParameterMetaData = {
    statementCache.getOrElseUpdate(sql, stmt.getParameterMetaData)
  }
}
