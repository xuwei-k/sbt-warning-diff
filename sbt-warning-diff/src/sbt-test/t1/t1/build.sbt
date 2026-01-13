val commonSettings = Def.settings(
  scalaVersion := "3.8.0",
  scalacOptions += "-deprecation"
)

val a1 = project.settings(commonSettings)
val a2 = project.settings(commonSettings)
