package warning_diff.rdf

trait LocationCompat { self: Location =>
  final def toTupleOption = Location.unapply(self)
}
