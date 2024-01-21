package warning_diff

import sjsonnew.Builder
import sjsonnew.JsonWriter
import sjsonnew.support.scalajson.unsafe.PrettyPrinter

class JsonClassOps[A](private val self: A) extends AnyVal {
  def toJsonString(implicit format: JsonWriter[A]): String = {
    val builder = new Builder(sjsonnew.support.scalajson.unsafe.Converter.facade)
    format.write(self, builder)
    PrettyPrinter.apply(
      builder.result.getOrElse(sys.error("invalid json"))
    )
  }
}

object JsonClassOps {
  implicit def toJsonClassOps[A](self: A): JsonClassOps[A] = new JsonClassOps[A](self)
}
