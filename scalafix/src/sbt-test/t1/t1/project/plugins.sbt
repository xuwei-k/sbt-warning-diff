addSbtPlugin("com.github.xuwei-k" % "warning-diff-scalafix-plugin" % System.getProperty("plugin.version"))

libraryDependencies ++= {
  sbtBinaryVersion.value match {
    case "2" =>
      Nil
    case "1.0" =>
      Seq(
        Defaults.sbtPluginExtra(
          "com.eed3si9n" % "sbt-projectmatrix" % "0.11.0",
          sbtBinaryVersion.value,
          scalaBinaryVersion.value,
        )
      )
  }
}
