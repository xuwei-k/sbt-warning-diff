package warning_diff

import sjsonnew.BasicJsonProtocol.*
import sjsonnew.JsonFormat
import warning_diff.FixInput.SubProject
import warning_diff.JsonClassOps.*

case class FixInput(
    projects: Seq[SubProject],
    base: String,
    output: String
) extends FixInputCompat {
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
  ) extends SubProjectCompat {
    override def toString = this.toJsonString
  }

  object SubProject {
    implicit val instance: JsonFormat[SubProject] =
      caseClass7(apply, (_: SubProject).toTupleOption)(
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
    caseClass3(apply, (_: FixInput).toTupleOption)(
      "projects",
      "base",
      "output"
    )
}
