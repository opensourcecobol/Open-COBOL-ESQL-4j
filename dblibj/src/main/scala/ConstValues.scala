import jp.osscons.opensourcecobol.libcobj.data.CobolDataStorage

object ConstValues {
  val RESULT_FAILED: Int = -1
  val RESULT_SUCCESS: Int = 0
  val RESULT_ERROR: Int = -2
  val RESULT_FLAGBASE: Int = 10
  val RESULT_FLAG1_PGSQL_DUMMYOPEN: Int = 11

  val OCDB_RES_DEFAULT_ADDRESS: Int = 0

  val DEFAULT_NEXT_CONN_ID: Int = 1
  val INVALID_CONN_ID: Int = -1
  val OCDB_INVALID_NUMBER:Int = -1
  val OCDB_INVALID_STRING: Int = 0
  val OCDB_CONN_CONNECT_OK: Int = 1
  val OCDB_CONN_NOT_CONNECT: Int = 0
  val OCDB_CONN_FAIL_CONNECT: Int = -1

  val OCPG_NO_ERROR = 0
  val OCPG_NOT_FOUND = 100
  val OCPG_OUT_OF_MEMORY = -12
  val OCPG_UNSUPPORTED = -200
  val OCPG_TOO_MANY_ARGUMENTS = -201
  val OCPG_TOO_FEW_ARGUMENTS = -202
  val OCPG_TOO_MANY_MATCHES = -203
  val OCPG_DATA_FORMAT_ERROR = -204
  val OCPG_INT_FORMAT = -204
  val OCPG_UINT_FORMAT = -205
  val OCPG_FLOAT_FORMAT = -206
  val OCPG_NUMERIC_FORMAT = -207
  val OCPG_INTERVAL_FORMAT = -208
  val OCPG_DATE_FORMAT = -209
  val OCPG_TIMESTAMP_FORMAT = -210
  val OCPG_CONVERT_BOOL = -211
  val OCPG_EMPTY = -212
  val OCPG_MISSING_INDICATOR = -213
  val OCPG_NO_ARRAY = -214
  val OCPG_DATA_NOT_ARRAY = -215
  val OCPG_NO_CONN = -220
  val OCPG_NOT_CONN = -221
  val OCPG_INVALID_STMT = -230
  val OCPG_INFORMIX_DUPLICATE_KEY = -239
  val OCPG_UNKNOWN_DESCRIPTOR = -240
  val OCPG_INVALID_DESCRIPTOR_INDEX = -241
  val OCPG_UNKNOWN_DESCRIPTOR_ITEM = -242
  val OCPG_VAR_NOT_NUMERIC = -243
  val OCPG_VAR_NOT_CHAR = -244
  val OCPG_INFORMIX_SUBSELECT_NOT_ONE = -284
  val OCPG_PGSQL = -400
  val OCPG_TRANS = -401
  val OCPG_CONNECT = -402
  val OCPG_DUPLICATE_KEY = -403
  val OCPG_SUBSELECT_NOT_ONE = -404
  val OCPG_WARNING_UNKNOWN_PORTAL = -602
  val OCPG_WARNING_IN_TRANSACTION = -603
  val OCPG_WARNING_NO_TRANSACTION = -604
  val OCPG_WARNING_PORTAL_EXISTS = -605
  val OCPG_LOCK_ERROR = -606
  val OCPG_JDD_ERROR = -607

  val OCDB_UNKNOWN_ERROR = -9999

  sealed trait SqlCode
  case class OCDB_NO_ERROR() extends SqlCode
  case class OCDB_NOT_FOUND() extends SqlCode
  case class OCDB_OUT_OF_MEMORY() extends SqlCode
  case class OCDB_UNSUPPORTED() extends SqlCode
  case class OCDB_TOO_MANY_ARGUMENTS() extends SqlCode
  case class OCDB_TOO_FEW_ARGUMENTS() extends SqlCode
  case class OCDB_TOO_MANY_MATCHES() extends SqlCode
  case class OCDB_DATA_FORMAT_ERROR() extends SqlCode
  case class OCDB_INT_FORMAT() extends SqlCode
  case class OCDB_UINT_FORMAT() extends SqlCode
  case class OCDB_FLOAT_FORMAT() extends SqlCode
  case class OCDB_NUMERIC_FORMAT() extends SqlCode
  case class OCDB_INTERVAL_FORMAT() extends SqlCode
  case class OCDB_DATE_FORMAT() extends SqlCode
  case class OCDB_TIMESTAMP_FORMAT() extends SqlCode
  case class OCDB_CONVERT_BOOL() extends SqlCode
  case class OCDB_EMPTY() extends SqlCode
  case class OCDB_MISSING_INDICATOR() extends SqlCode
  case class OCDB_NO_ARRAY() extends SqlCode
  case class OCDB_DATA_NOT_ARRAY() extends SqlCode
  case class OCDB_NO_CONN() extends SqlCode
  case class OCDB_NOT_CONN() extends SqlCode
  case class OCDB_INVALID_STMT() extends SqlCode
  case class OCDB_INFORMIX_DUPLICATE_KEY() extends SqlCode
  case class OCDB_UNKNOWN_DESCRIPTOR() extends SqlCode
  case class OCDB_INVALID_DESCRIPTOR_INDEX() extends SqlCode
  case class OCDB_UNKNOWN_DESCRIPTOR_ITEM() extends SqlCode
  case class OCDB_VAR_NOT_NUMERIC() extends SqlCode
  case class OCDB_VAR_NOT_CHAR() extends SqlCode
  case class OCDB_INFORMIX_SUBSELECT_NOT_ONE() extends SqlCode
  case class OCDB_PGSQL() extends SqlCode
  case class OCDB_TRANS() extends SqlCode
  case class OCDB_CONNECT() extends SqlCode
  case class OCDB_DUPLICATE_KEY() extends SqlCode
  case class OCDB_SUBSELECT_NOT_ONE() extends SqlCode
  case class OCDB_WARNING_UNKNOWN_PORTAL() extends SqlCode
  case class OCDB_WARNING_IN_TRANSACTION() extends SqlCode
  case class OCDB_WARNING_NO_TRANSACTION() extends SqlCode
  case class OCDB_WARNING_PORTAL_EXISTS() extends SqlCode
  case class OCDB_DEFAULT_DBNAMES() extends SqlCode
  case class OCDB_LOCK_ERROR() extends SqlCode
  case class OCDB_JDD_ERROR() extends SqlCode

  val SQL_SAVEPOINT: String  = "SAVEPOINT oc_save"
  val SQL_RELEASE_SAVEPOINT: String = "RELEASE SAVEPOINT oc_save"
  val SQL_ROLLBACK_SAVEPOINT: String = "ROLLBACK TO oc_save"

  val OCESQL_DEFAULT_DB_NAME: String = "OCDB_DEFAULT_DBNAME"

  val OCDB_TYPE_UNSIGNED_NUMBER: Int = 1         // 符号無数字
  val OCDB_TYPE_SIGNED_NUMBER_TC: Int = 3        // 符号付数字(trailing combined)
  val OCDB_TYPE_SIGNED_NUMBER_LS: Int = 4        // 符号付数字(leading separete)
  val OCDB_TYPE_UNSIGNED_NUMBER_PD: Int = 8        // 正のパック10進数
  val OCDB_TYPE_SIGNED_NUMBER_PD: Int = 9      // 符号付パック10進数
  val OCDB_TYPE_SINGED_BINARY_NATIVE: Int = 13   // 符号付2進数(native-order)
  val OCDB_TYPE_UNSINGED_BINARY_NATIVE: Int = 14 // 符号無2進数(native-order)
  val OCDB_TYPE_ALPHANUMERIC: Int = 16           // 英数字
  val OCDB_TYPE_GROUP: Int = 22                  // 集団
  val OCDB_TYPE_JAPANESE: Int = 24               // 日本語
  val OCDB_TYPE_ALPHANUMERIC_VARYING: Int = 30     // VARYING(PIC X)
  val OCDB_TYPE_JAPANESE_VARYING: Int = 31         // VARYING(PIC N)
  val OCDB_TYPE_MIN: Int = 0
  val OCDB_TYPE_MAX: Int = 32

  val SIGN_LENGTH: Int = 1
  val TERMINAL_LENGTH: Int = 1
  val DECIMAL_LENGTH: Int = 1

  val OCDB_CURSOR_WITH_HOLD_OFF: Boolean = false
  val OCDB_CURSOR_WITH_HOLD_ON: Boolean = true

  val nullDataStorage = new CobolDataStorage(0)

  val TYPE_TC_NEGATIVE_FINAL_NUMBER: String = "pqrstuvwxy"
  val TYPE_TC_NEGATIVE_FINAL_NUMBER_LEN = TYPE_TC_NEGATIVE_FINAL_NUMBER.length

  sealed trait ReadDirection
  case class OCDB_READ_NEXT() extends ReadDirection
  case class OCDB_READ_PREVIOUS() extends ReadDirection
  case class OCDB_READ_CURRENT() extends ReadDirection
}
