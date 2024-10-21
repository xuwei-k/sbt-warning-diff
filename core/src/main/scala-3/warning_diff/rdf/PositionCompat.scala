package warning_diff.rdf

trait PositionCompat { self: Position =>
  final def toTupleOption = Option(Tuple.fromProductTyped(self))
}
