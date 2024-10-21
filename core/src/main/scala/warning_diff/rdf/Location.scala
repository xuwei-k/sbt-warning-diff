package warning_diff.rdf

import sjsonnew.BasicJsonProtocol.*
import sjsonnew.JsonFormat
import warning_diff.JsonClassOps.*

case class Location(
  path: String,
  range: Range
) extends LocationCompat {
  override def toString = this.toJsonString
}

object Location {
  implicit val instance: JsonFormat[Location] =
    caseClass2(apply, (_: Location).toTupleOption)(
      "path",
      "range"
    )
}
