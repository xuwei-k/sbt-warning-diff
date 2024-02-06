package warning_diff

import sjsonnew.BasicJsonProtocol.*
import sjsonnew.JsonFormat
import warning_diff.FixInput.SubProject
import warning_diff.JsonClassOps.*

case class FixInput(
  projects: Seq[SubProject],
  base: String,
  output: String
) {
  override def toString = this.toJsonString
}

object FixInput {
  case class SubProject(
    projectId: String,
    sbtConfig: String,
    scalafixConfig: String,
    sources: Seq[String],
    scalacOptions: Seq[String],
    scalaVersion: String,
    dialect: Dialect
  ) {
    override def toString = this.toJsonString
  }

  object SubProject {
    implicit val instance: JsonFormat[SubProject] =
      caseClass7(apply, unapply)(
        "project-id",
        "sbt-config",
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
