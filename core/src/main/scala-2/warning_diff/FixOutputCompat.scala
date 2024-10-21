package warning_diff

trait FixOutputCompat { self: FixOutput =>
  final def toTupleOption = FixOutput.unapply(self)
}
