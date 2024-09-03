package warning_diff.rdf

import sjsonnew.BasicJsonProtocol.*
import sjsonnew.JsonFormat

case class Diagnostic(
  message: String,
  location: Location,
  severity: Option[Severity]
)

object Diagnostic {
  implicit val instance: JsonFormat[Diagnostic] =
    caseClass3(apply, unapply)(
      "message",
      "location",
      "severity"
    )
}
