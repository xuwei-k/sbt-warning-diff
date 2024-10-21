package warning_diff.rdf

trait DiagnosticCompat { self: Diagnostic =>
  final def toTupleOption = Option(Tuple.fromProductTyped(self))
}
