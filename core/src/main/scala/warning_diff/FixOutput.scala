package warning_diff

import sjsonnew.BasicJsonProtocol.*
import sjsonnew.JsonFormat

case class FixOutput(
  projectId: String,
  sbtConfig: String,
  warnings: Seq[Warning]
)

object FixOutput {
  implicit val instance: JsonFormat[FixOutput] =
    caseClass3(apply, unapply)(
      "project-id",
      "sbt-config",
      "warnings"
    )

}
