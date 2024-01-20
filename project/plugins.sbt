libraryDependencies += "org.scala-sbt" %% "scripted-plugin" % sbtVersion.value
addSbtPlugin("com.eed3si9n" % "sbt-buildinfo" % "0.11.0")
addSbtPlugin("org.scalameta" % "sbt-scalafmt" % "2.5.2")
addSbtPlugin("org.xerial.sbt" % "sbt-sonatype" % "3.9.21")
addSbtPlugin("com.github.sbt" % "sbt-release" % "1.3.0")
addSbtPlugin("com.github.sbt" % "sbt-pgp" % "2.2.1")
addSbtPlugin("com.eed3si9n" % "sbt-projectmatrix" % "0.9.2")
addSbtPlugin("com.github.xuwei-k" % "sbt-warning-diff" % "0.1.1")
addSbtPlugin("ch.epfl.scala" % "sbt-scalafix" % "0.11.1")

scalacOptions ++= Seq(
  "-deprecation",
  "-unchecked",
  "-language:higherKinds"
)

Compile / scalacOptions -= "-Xsource:3"
