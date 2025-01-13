package warning_diff.rdf

import sjsonnew.BasicJsonProtocol.*
import sjsonnew.JsonFormat
import warning_diff.JsonClassOps.*

case class Range(
    start: Position,
    end: Position
) extends RangeCompat {
  override def toString = this.toJsonString
}

object Range {
  implicit val instance: JsonFormat[Range] =
    caseClass2(apply, (_: Range).toTupleOption)(
      "start",
      "end"
    )
}
