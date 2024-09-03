package warning_diff.rdf

case class DiagnosticResult(
  diagnostics: Seq[Diagnostic],
  source: Option[Source]
)

object DiagnosticResult {
  def fromWarnings(values: Seq[warning_diff.Warning]): DiagnosticResult =
    DiagnosticResult(
      diagnostics = values.map { w =>
        Diagnostic(
          message = w.message,
          location = Location(
            path = w.position.sourcePath.getOrElse(""),
            range = Range(
              line = w.position.line,
              column = w.position.startColumn
            )
          ),
          severity = Some(Severity.Warning)
        )
      },
      source = Some(
        Source(
          name = "Scala"
        )
      )
    )
}
