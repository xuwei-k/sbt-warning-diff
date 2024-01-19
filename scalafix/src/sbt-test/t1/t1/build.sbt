val commonSettings = Def.settings(
  scalaVersion := "2.13.12",
  scalacOptions += "-deprecation"
)

val a1 = project.settings(commonSettings).enablePlugins(ScalafixPlugin)
val a2 = project.settings(commonSettings).enablePlugins(ScalafixPlugin)

ThisBuild / scalafixDependencies += "com.github.xuwei-k" %% "scalafix-rules" % "0.3.4"
