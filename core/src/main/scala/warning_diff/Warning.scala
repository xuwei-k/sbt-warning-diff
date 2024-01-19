package warning_diff

import sjsonnew.BasicJsonProtocol
import sjsonnew.BasicJsonProtocol._
import sjsonnew.JsonFormat
import xsbti.Problem

case class Warning(message: String, position: Pos) {
  import JsonClassOps._
  override def toString = this.toJsonString
}

object Warning {
  def fromSbt(p: Problem): Warning = {
    Warning(
      message = p.message().replaceAll("\u001B\\[[;\\d]*m", ""),
      position = Pos.fromSbt(p.position())
    )
  }
  implicit val instance: JsonFormat[Warning] = {
    implicit val position: JsonFormat[Pos] = BasicJsonProtocol.caseClass12(Pos.apply, Pos.unapply)(
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
    caseClass2(Warning.apply, Warning.unapply)(
      "message",
      "position"
    )
  }
}
