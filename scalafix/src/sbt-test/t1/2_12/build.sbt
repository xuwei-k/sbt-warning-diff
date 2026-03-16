val baseSettings = Def.settings(
  scalaVersion := "2.12.21",
  scalacOptions += "-deprecation"
)

val a1 = project
  .settings(baseSettings)

val myScalafix = project
  .disablePlugins(ScalafixPlugin)
  .enablePlugins(ScalafixRuleResourceGen)
  .settings(
    baseSettings,
    libraryDependencies += "ch.epfl.scala" %% "scalafix-core" % _root_.scalafix.sbt.BuildInfo.scalafixVersion,
    InputKey[Unit]("check") := {
      val rules = scalafixRuleResourceGenRuleNames.value
      assert(rules.size == 1, rules)
    },
  )

baseSettings

ThisBuild / scalafixDependencies += "com.github.xuwei-k" %% "scalafix-rules" % System.getProperty("xuwei.scalafix-rules.version")
