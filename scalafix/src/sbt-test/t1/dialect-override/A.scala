package example

import scala.language.experimental.captureChecking

object A {
  def foo[B, C](f: B^ => C): C =
    ???
}
