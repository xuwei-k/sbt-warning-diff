def Scala3 = "3.7.3"

val baseSettings = Def.settings(
  scalaVersion := Scala3,
  ThisBuild / scalaVersion := Scala3,
  scalacOptions += "-deprecation"
)

val a1 = project
  .settings(
    baseSettings
  )

val myScalafix = project
  .disablePlugins(ScalafixPlugin)
  .enablePlugins(ScalafixRuleResourceGen)
  .settings(
    baseSettings,
    scalaVersion := "2.13.17",
    libraryDependencies += "ch.epfl.scala" %% "scalafix-core" % _root_.scalafix.sbt.BuildInfo.scalafixVersion,
    Compile / resources := {
      val output = (Compile / resourceManaged).value / "META-INF" / "services" / "scalafix.v1.Rule"
      val rules = IO.readLines(output)
      assert(rules == Seq("fix.FindHoge"), rules)
      (Compile / resources).value
    }
  )

baseSettings

ThisBuild / scalafixDependencies += "com.github.xuwei-k" %% "scalafix-rules" % System.getProperty("xuwei.scalafix-rules.version")
