package warning_diff.rdf

import sjsonnew.BasicJsonProtocol.*
import sjsonnew.JsonFormat

case class Location(
  path: String,
  range: Range
)

object Location {
  implicit val instance: JsonFormat[Location] =
    caseClass2(apply, unapply)(
      "path",
      "range"
    )
}
