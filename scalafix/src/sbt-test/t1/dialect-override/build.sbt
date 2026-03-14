scalaVersion := "3.7.4"

ThisBuild / scalafixDependencies += "com.github.xuwei-k" %% "scalafix-rules" % System.getProperty("xuwei.scalafix-rules.version")

InputKey[Unit]("updateScalafixConfigDialectOverride") := {
  IO.append(
    file(".scalafix.conf"),
    Seq(
      "",
      "dialectOverride.allowCaptureChecking = true"
    ).mkString("\n")
  )
}
