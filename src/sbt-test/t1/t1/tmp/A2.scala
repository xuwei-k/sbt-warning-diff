class A2 {
  def x2[X1, X2](m: Map[X1, X2]) = m.mapValues(a => a)

  def x3: Seq[Int] = Array(3)
}
