package warning_diff.rdf

import sjsonnew.BasicJsonProtocol.*
import sjsonnew.Builder
import sjsonnew.JsonFormat
import sjsonnew.Unbuilder

sealed abstract class Severity(val value: String) extends Product with Serializable

object Severity {
  case object Unknown extends Severity("UNKNOWN_SEVERITY")
  case object Error extends Severity("ERROR")
  case object Warning extends Severity("WARNING")
  case object Info extends Severity("INFO")

  private val values: Seq[Severity] = Seq(
    Unknown,
    Error,
    Warning,
    Info
  )

  private val map: Map[String, Severity] = values.map(x => x.value -> x).toMap

  implicit val instance: JsonFormat[Severity] =
    new JsonFormat[Severity] {
      private val stringInstance = implicitly[JsonFormat[String]]

      override def write[J](obj: Severity, builder: Builder[J]): Unit =
        stringInstance.write(obj.value, builder)

      override def read[J](jsOpt: Option[J], unbuilder: Unbuilder[J]): Severity =
        map(stringInstance.read(jsOpt, unbuilder))
    }
}
