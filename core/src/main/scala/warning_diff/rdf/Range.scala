package warning_diff.rdf

import sjsonnew.BasicJsonProtocol.*
import sjsonnew.JsonFormat

case class Range(
  line: Option[Int],
  column: Option[Int]
)

object Range {
  implicit val instance: JsonFormat[Range] =
    caseClass2(apply, unapply)(
      "line",
      "column"
    )
}
