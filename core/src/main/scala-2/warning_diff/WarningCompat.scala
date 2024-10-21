package warning_diff

trait WarningCompat { self: Warning =>
  final def toTupleOption = Warning.unapply(self)
}
