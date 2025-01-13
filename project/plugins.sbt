libraryDependencies += "org.scala-sbt" %% "scripted-plugin" % sbtVersion.value
addSbtPlugin("com.eed3si9n" % "sbt-buildinfo" % "0.13.1")
addSbtPlugin("org.scalameta" % "sbt-scalafmt" % "2.5.3")
addSbtPlugin("org.xerial.sbt" % "sbt-sonatype" % "3.11.3")
addSbtPlugin("com.github.sbt" % "sbt-release" % "1.4.0")
addSbtPlugin("com.github.sbt" % "sbt-pgp" % "2.3.1")
addSbtPlugin("com.eed3si9n" % "sbt-projectmatrix" % "0.10.1")
addSbtPlugin("com.github.xuwei-k" % "warning-diff-scalafix-plugin" % "0.5.0")

scalacOptions ++= Seq(
  "-deprecation",
  "-unchecked",
  "-language:higherKinds"
)
