package warning_diff

import sjsonnew.BasicJsonProtocol.*
import sjsonnew.JsonFormat
import warning_diff.JsonClassOps.*

case class FixOutput(
  projectId: String,
  sbtConfig: String,
  warnings: Seq[Warning]
) {
  override def toString = this.toJsonString
}

object FixOutput {
  implicit val instance: JsonFormat[FixOutput] =
    caseClass3(apply, unapply)(
      "project-id",
      "sbt-config",
      "warnings"
    )

}
