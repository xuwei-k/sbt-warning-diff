val baseSettings = Def.settings(
  scalaVersion := "2.12.20",
  scalacOptions += "-deprecation"
)

val a1 = project
  .settings(baseSettings)

val myScalafix = project
  .disablePlugins(ScalafixPlugin)
  .settings(
    baseSettings,
    libraryDependencies += "ch.epfl.scala" %% "scalafix-core" % _root_.scalafix.sbt.BuildInfo.scalafixVersion,
    Compile / resourceGenerators += Def.task {
      val rules = (Compile / compile).value
        .asInstanceOf[sbt.internal.inc.Analysis]
        .apis
        .internal
        .collect {
          case (className, analyzed)
              if analyzed.api.classApi.structure.parents
                .collect {
                  case p: xsbti.api.Projection => p.id
                }
                .exists(Set("SyntacticRule", "SemanticRule")) =>
            className
        }
        .toList
        .sorted
      assert(rules.size == 1, rules)
      val output = (Compile / resourceManaged).value / "META-INF" / "services" / "scalafix.v1.Rule"
      IO.writeLines(output, rules)
      Seq(output)
    }.taskValue
  )

baseSettings

ThisBuild / scalafixDependencies += "com.github.xuwei-k" %% "scalafix-rules" % "0.6.4"
