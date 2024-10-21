package warning_diff

trait PosCompat { self: Pos =>
  final def toTupleOption = Pos.unapply(self)
}
