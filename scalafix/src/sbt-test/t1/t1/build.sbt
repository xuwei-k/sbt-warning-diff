def Scala212 = "2.12.21"
def Scala213 = "2.13.18"
def Scala3 = "3.3.6"

val baseSettings = Def.settings(
  scalacOptions += "-deprecation"
)

val commonSettings = Def.settings(
  baseSettings,
  scalaVersion := Scala213
)

val a1 = projectMatrix
  .settings(baseSettings)
  .defaultAxes(VirtualAxis.jvm)
  .jvmPlatform(
    Seq(Scala212, Scala3)
  )
  .settings(
    Compile / scalafix / unmanagedSources := {
      (Compile / scalafix / unmanagedSources).value.filterNot(_.getName == "A3.scala")
    }
  )

val a2 = project.settings(commonSettings)

val myScalafix = projectMatrix
  .defaultAxes(VirtualAxis.jvm)
  .jvmPlatform(
    Seq(Scala212, Scala213)
  )
  .in(file("myScalafix"))
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

val root = project
  .in(file("."))
  .settings(
    commonSettings
  )
  .aggregate(a2)
  .aggregate(
    Seq(
      a1,
      myScalafix
    ).flatMap(_.projectRefs)*
  )

ThisBuild / scalafixDependencies += "com.github.xuwei-k" %% "scalafix-rules" % System.getProperty("xuwei.scalafix-rules.version")
