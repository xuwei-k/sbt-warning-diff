package warning_diff.rdf

import sjsonnew.BasicJsonProtocol.*
import sjsonnew.JsonFormat

case class Code(
  value: String,
  url: Option[String]
)

object Code {
  implicit val instance: JsonFormat[Code] =
    caseClass2(apply, unapply)(
      "value",
      "url"
    )
}
