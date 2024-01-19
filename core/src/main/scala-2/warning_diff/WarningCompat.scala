package warning_diff

abstract class WarningCompat { self: Warning.type =>
  val tupleOpt: Warning => Option[(String, Pos)] = this.unapply
}
