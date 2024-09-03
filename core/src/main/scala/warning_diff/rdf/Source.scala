package warning_diff.rdf

import sjsonnew.BasicJsonProtocol.*
import sjsonnew.JsonFormat

case class Source(
  name: String
)

object Source {
  implicit val instance: JsonFormat[Source] =
    caseClass1(apply, unapply)(
      "name"
    )
}
