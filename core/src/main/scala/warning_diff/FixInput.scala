package warning_diff

import sjsonnew.BasicJsonProtocol._
import sjsonnew.JsonFormat

case class FixInput(
  scalafixConfig: String,
  sources: Seq[String],
  output: String
)

object FixInput {
  implicit val instance: JsonFormat[FixInput] =
    caseClass3(FixInput.apply, FixInput.unapply)(
      "scalafix-config",
      "sources",
      "output"
    )
}
