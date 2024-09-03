package warning_diff.rdf

case class DiagnosticResult(
  diagnostics: Seq[Diagnostic],
  source: Option[Source],
  severity: Option[Severity]
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
          severity = Some(Severity.Warning),
          source = None,
          code = None,
          suggestions = Nil,
          original_output = None,
          related_locations = Nil
        )
      },
      source = Some(
        Source(
          name = "Scala",
          url = None
        )
      ),
      severity = None
    )
}
