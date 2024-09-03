package warning_diff.rdf

import sjsonnew.BasicJsonProtocol.*
import sjsonnew.JsonFormat
import warning_diff.JsonClassOps.*
import xsbti.Severity

case class DiagnosticResult(
  diagnostics: Seq[Diagnostic],
  source: Option[Source]
) {
  override def toString = this.toJsonString
}

object DiagnosticResult {
  def fromSbt(values: Seq[xsbti.Problem]): DiagnosticResult = {
    def j2s(j: java.util.Optional[Integer]): Option[Int] =
      if (j.isPresent) Option(j.get.asInstanceOf[Int]) else None

    DiagnosticResult(
      diagnostics = values.map { w =>
        Diagnostic(
          message = w.message,
          location = Location(
            path = {
              val prefix = "${BASE}/"
              w.position.sourcePath
                .map { value =>
                  if (value.startsWith(prefix)) {
                    value.drop(prefix.length)
                  } else {
                    value
                  }
                }
                .orElse("")
            },
            range = Range(
              start = Position(
                line = j2s(w.position.startLine).orElse(j2s(w.position.line)),
                column = j2s(w.position.startColumn)
              ),
              end = Position(
                line = j2s(w.position.endLine()).orElse(j2s(w.position.line)),
                column = j2s(w.position.endColumn)
              )
            )
          ),
          severity = Some {
            w.severity() match {
              case Severity.Info => "INFO"
              case Severity.Warn => "WARNING"
              case Severity.Error => "ERROR"
            }
          }
        )
      },
      source = Some(
        Source(
          name = "scala"
        )
      )
    )
  }

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
                column = w.position.endColumn
              )
            )
          ),
          severity = Some("WARNING")
        )
      },
      source = Some(
        Source(
          name = "scala"
        )
      )
    )

  implicit val instance: JsonFormat[DiagnosticResult] =
    caseClass2(apply, unapply)(
      "diagnostics",
      "source"
    )
}
