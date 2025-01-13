package warning_diff.rdf

import sjsonnew.BasicJsonProtocol.*
import sjsonnew.JsonFormat
import warning_diff.JsonClassOps.*

case class Source(
    name: String
) {
  override def toString = this.toJsonString
}

object Source {
  implicit val instance: JsonFormat[Source] =
    caseClass1(apply, (x: Source) => Option(x.name))(
      "name"
    )
}
