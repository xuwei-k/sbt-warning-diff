libraryDependencies += "com.github.xuwei-k" %% "scala-version-from-sbt-version" % "0.1.0"
libraryDependencies += "org.scala-sbt" %% "scripted-plugin" % sbtVersion.value
libraryDependencies += "io.get-coursier" %% "coursier" % "2.1.26"
addSbtPlugin("com.eed3si9n" % "sbt-buildinfo" % "0.14.0")
addSbtPlugin("org.scalameta" % "sbt-scalafmt" % "2.6.2")
addSbtPlugin("com.github.sbt" % "sbt-release" % "1.6.0")
addSbtPlugin("com.github.sbt" % "sbt-pgp" % "2.4.0")
addSbtPlugin("com.github.xuwei-k" % "warning-diff-scalafix-plugin" % "0.6.7")

scalacOptions ++= Seq(
  "-deprecation",
  "-unchecked"
)
