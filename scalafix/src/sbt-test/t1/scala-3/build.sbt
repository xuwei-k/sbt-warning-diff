def Scala3 = "3.8.0"

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
    scalaVersion := _root_.scalafix.sbt.BuildInfo.scala213,
    libraryDependencies += "ch.epfl.scala" %% "scalafix-core" % _root_.scalafix.sbt.BuildInfo.scalafixVersion,
    Compile / resources := {
      val output = (Compile / resourceManaged).value / "META-INF" / "services" / "scalafix.v1.Rule"
      val rules = IO.readLines(output)
      assert(rules == Seq("fix.FindHoge"), rules)
      (Compile / resources).value
    }
  )

lazy val root = project.in(file("."))
  .aggregate(a1, myScalafix)
  .settings(
    baseSettings,
    ThisBuild / scalafixDependencies += "com.github.xuwei-k" %% "scalafix-rules" % System.getProperty("xuwei.scalafix-rules.version"),
    InputKey[Unit]("check") := {
      val expect = IO.read(file("1.json"))
      val actual = IO.read(file(
        sbtBinaryVersion.value match {
          case "1.0" =>
            "target/warnings/warnings.json"
          case "2" =>
            s"target/out/jvm/scala-${scalaVersion.value}/${name.value}/warnings/warnings.json"
        }
      ))
      assert(expect == actual, s"$expect != $actual")
    }
  )
