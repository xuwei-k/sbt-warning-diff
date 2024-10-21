package warning_diff

trait FixInputCompat { self: FixInput =>
  final def toTupleOption = Option(Tuple.fromProductTyped(self))
}
