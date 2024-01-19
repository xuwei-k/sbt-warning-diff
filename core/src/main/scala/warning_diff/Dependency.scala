package warning_diff

import sjsonnew.JsonFormat
import sjsonnew.BasicJsonProtocol._

case class Dependency(
  groupId: String,
  artifactId: String,
  version: String
)

object Dependency {
  implicit val instance: JsonFormat[Dependency] =
    caseClass3(apply, unapply)(
      "groupId",
      "artifactId",
      "version"
    )
}
