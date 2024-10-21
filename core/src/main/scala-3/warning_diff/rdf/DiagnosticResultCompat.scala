package warning_diff.rdf

trait DiagnosticResultCompat { self: DiagnosticResult =>
  final def toTupleOption = Option(Tuple.fromProductTyped(self))
}
