package warning_diff

trait WarningCompat { self: Warning =>
  final def toTupleOption = Option(Tuple.fromProductTyped(self))
}
