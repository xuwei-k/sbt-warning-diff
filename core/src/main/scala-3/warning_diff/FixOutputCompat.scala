package warning_diff

trait FixOutputCompat { self: FixOutput =>
  final def toTupleOption = Option(Tuple.fromProductTyped(self))
}
