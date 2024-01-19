package warning_diff

abstract class PosCompat { self: Pos.type =>
  val tupleOpt: Pos => Option[
    (
      Option[Int],
      String,
      Option[Int],
      Option[Int],
      Option[String],
      Option[String],
      Option[Int],
      Option[Int],
      Option[Int],
      Option[Int],
      Option[Int],
      Option[Int]
    )
  ] = this.unapply
}
