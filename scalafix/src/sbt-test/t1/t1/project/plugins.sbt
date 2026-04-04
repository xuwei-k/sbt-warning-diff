addSbtPlugin("com.github.xuwei-k" % "sbt-root-aggregate" % "0.1.0")

addSbtPlugin("com.github.xuwei-k" % "warning-diff-scalafix-plugin" % System.getProperty("plugin.version"))

addSbtPlugin("com.github.xuwei-k" % "scalafix-rule-resource-gen" % "0.1.2")

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
