package warning_diff

private[warning_diff] object WarningDiffPluginCompat {
  implicit class DefOps(private val self: sbt.Def.type) extends AnyVal {
    def uncached[A](a: A): A = a
  }
}
