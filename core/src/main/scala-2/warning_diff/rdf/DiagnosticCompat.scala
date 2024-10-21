package warning_diff.rdf

trait DiagnosticCompat { self: Diagnostic =>
  final def toTupleOption = Diagnostic.unapply(self)
}
