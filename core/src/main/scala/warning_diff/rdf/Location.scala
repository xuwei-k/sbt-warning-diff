package warning_diff.rdf

import sjsonnew.BasicJsonProtocol.*
import sjsonnew.JsonFormat
import warning_diff.JsonClassOps.*

case class Location(
  path: String,
  range: Range
) {
  override def toString = this.toJsonString
}

object Location {
  implicit val instance: JsonFormat[Location] =
    caseClass2(apply, unapply)(
      "path",
      "range"
    )
}
