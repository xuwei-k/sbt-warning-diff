package warning_diff

trait FixInputCompat { self: FixInput =>
  final def toTupleOption = FixInput.unapply(self)
}
