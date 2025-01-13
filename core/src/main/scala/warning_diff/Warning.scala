package warning_diff

import sjsonnew.BasicJsonProtocol
import sjsonnew.BasicJsonProtocol.*
import sjsonnew.JsonFormat
import xsbti.Problem
import xsbti.Severity

case class Warning(message: String, position: Pos, severity: Option[String]) extends WarningCompat {
  import JsonClassOps.*
  override def toString = this.toJsonString
}

object Warning {
  def apply(
      message: String,
      position: Pos
  ): Warning = apply(
    message = message,
    position = position,
    severity = None
  )

  def fromSbt(p: Problem): Warning = {
    Warning(
      message = p.message().replaceAll("\u001B\\[[;\\d]*m", ""),
      position = Pos.fromSbt(p.position()),
      severity = p.severity() match {
        case Severity.Warn => None
        case Severity.Info => Some("INFO")
        case Severity.Error => Some("ERROR")
      }
    )
  }
  implicit val instance: JsonFormat[Warning] = {
    implicit val position: JsonFormat[Pos] = BasicJsonProtocol.caseClass12(Pos.apply, (_: Pos).toTupleOption)(
      "line",
      "lineContent",
      "offset",
      "pointer",
      "pointerSpace",
      "sourcePath",
      "startOffset",
      "endOffset",
      "startLine",
      "startColumn",
      "endLine",
      "endColumn"
    )
    caseClass3(Warning.apply, (_: Warning).toTupleOption)(
      "message",
      "position",
      "severity"
    )
  }
}
