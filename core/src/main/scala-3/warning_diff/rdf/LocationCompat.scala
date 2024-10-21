package warning_diff.rdf

trait LocationCompat { self: Location =>
  final def toTupleOption = Option(Tuple.fromProductTyped(self))
}
