package warning_diff

trait SubProjectCompat { self: FixInput.SubProject =>
  final def toTupleOption = Option(Tuple.fromProductTyped(self))
}
