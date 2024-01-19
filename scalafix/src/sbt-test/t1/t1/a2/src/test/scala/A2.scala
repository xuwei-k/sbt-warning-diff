class A2 {
  def x2[X1, X2](m: Map[X1, X2]) = m.mapValues(a => a)
}

object A2 {
  implicit class Foo(val x: Int) {
    def bar: Int = x + 2
  }
}
