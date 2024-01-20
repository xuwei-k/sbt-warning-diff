package warning_diff

import sjsonnew.BasicJsonProtocol._
import sjsonnew.JsonFormat

case class FixInput(
  scalafixConfig: String,
  sources: Seq[String],
  base: String,
  output: String
)

object FixInput {
  implicit val instance: JsonFormat[FixInput] =
    caseClass4(FixInput.apply, FixInput.unapply)(
      "scalafix-config",
      "sources",
      "base",
      "output"
    )
}
