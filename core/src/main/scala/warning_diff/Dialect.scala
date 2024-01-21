package warning_diff

import sjsonnew.BasicJsonProtocol._
import sjsonnew.Builder
import sjsonnew.JsonFormat
import sjsonnew.Unbuilder

sealed abstract class Dialect(val value: String) extends Product with Serializable

object Dialect {
  case object Scala210 extends Dialect("Scala210")
  case object Scala211 extends Dialect("Scala211")
  case object Scala212 extends Dialect("Scala212")
  case object Scala213 extends Dialect("Scala213")
  case object Scala212Source3 extends Dialect("Scala212Source3")
  case object Scala213Source3 extends Dialect("Scala213Source3")
  case object Scala3 extends Dialect("Scala3")

  val all: Seq[Dialect] = Seq(
    Scala210,
    Scala211,
    Scala212,
    Scala213,
    Scala212Source3,
    Scala213Source3,
    Scala3
  )

  val map: Map[String, Dialect] = all.map(a => a.value -> a).toMap

  implicit val instance: JsonFormat[Dialect] =
    new JsonFormat[Dialect] {
      private val stringInstance = implicitly[JsonFormat[String]]

      override def write[J](obj: Dialect, builder: Builder[J]): Unit =
        stringInstance.write(obj.value, builder)

      override def read[J](jsOpt: Option[J], unbuilder: Unbuilder[J]): Dialect =
        map(stringInstance.read(jsOpt, unbuilder))
    }
}
