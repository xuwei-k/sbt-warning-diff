package warning_diff

trait SubProjectCompat { self: FixInput.SubProject =>
  final def toTupleOption = FixInput.SubProject.unapply(self)
}
