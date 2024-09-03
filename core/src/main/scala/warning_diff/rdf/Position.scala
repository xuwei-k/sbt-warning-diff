package warning_diff.rdf

import sjsonnew.BasicJsonProtocol.*
import sjsonnew.JsonFormat
import warning_diff.JsonClassOps.*

case class Position(
  line: Option[Int],
  column: Option[Int]
) {
  override def toString = this.toJsonString
}

object Position {
  implicit val instance: JsonFormat[Position] =
    caseClass2(apply, unapply)(
      "line",
      "column"
    )
}
