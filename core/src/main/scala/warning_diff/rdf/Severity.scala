package warning_diff.rdf

sealed abstract class Severity(val value: String) extends Product with Serializable

object Severity {
  case object Unknown extends Severity("UNKNOWN_SEVERITY")
  case object Error extends Severity("ERROR")
  case object Warning extends Severity("WARNING")
  case object Info extends Severity("INFO")
}
