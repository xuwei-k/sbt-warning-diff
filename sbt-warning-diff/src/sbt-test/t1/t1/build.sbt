val commonSettings = Def.settings(
  scalaVersion := "2.13.13",
  scalacOptions += "-deprecation"
)

val a1 = project.settings(commonSettings)
val a2 = project.settings(commonSettings)
