package warning_diff.rdf

import sjsonnew.BasicJsonProtocol.*
import sjsonnew.JsonFormat
import warning_diff.JsonClassOps.*

case class DiagnosticResult(
    diagnostics: Seq[Diagnostic],
    source: Option[Source]
) extends DiagnosticResultCompat {
  override def toString = this.toJsonString
}

object DiagnosticResult {
  def fromWarnings(values: Seq[warning_diff.Warning]): DiagnosticResult =
    DiagnosticResult(
      diagnostics = values.map { w =>
        Diagnostic(
          message = w.message,
          location = Location(
            path = {
              val prefix = "${BASE}/"
              w.position.sourcePath match {
                case Some(value) =>
                  if (value.startsWith(prefix)) {
                    value.drop(prefix.length)
                  } else {
                    value
                  }
                case None =>
                  ""
              }
            },
            range = Range(
              start = Position(
                line = w.position.startLine.orElse(w.position.line),
                column = w.position.startColumn
              ),
              end = Position(
                line = w.position.endLine.orElse(w.position.line),
                column = {
                  // on purpose set `None`
                  // don't use `w.position.endColumn`
                  None
                }
              )
            )
          ),
          severity = w.severity.orElse(Some("WARNING"))
        )
      }.distinct,
      source = Some(
        Source(
          name = "scala"
        )
      )
    )

  implicit val instance: JsonFormat[DiagnosticResult] =
    caseClass2(apply, (_: DiagnosticResult).toTupleOption)(
      "diagnostics",
      "source"
    )
}
