package warning_diff

import sjsonnew.BasicJsonProtocol._
import sjsonnew.JsonFormat
import warning_diff.FixInput.SubProject

case class FixInput(
  projects: Seq[SubProject],
  base: String,
  output: String
)

object FixInput {
  case class SubProject(
    scalafixConfig: String,
    sources: Seq[String],
    scalacOptions: Seq[String],
    scalaVersion: String,
    dialect: Dialect
  )

  object SubProject {
    implicit val instance: JsonFormat[SubProject] =
      caseClass5(apply, unapply)(
        "scalafix-config",
        "sources",
        "scalac-options",
        "scala-version",
        "dialect"
      )
  }

  implicit val instance: JsonFormat[FixInput] =
    caseClass3(apply, unapply)(
      "projects",
      "base",
      "output"
    )
}
