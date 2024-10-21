package warning_diff

trait PosCompat { self: Pos =>
  final def toTupleOption = Option(Tuple.fromProductTyped(self))
}
