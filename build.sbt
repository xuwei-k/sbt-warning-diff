import ReleaseTransformations.*

def Scala212 = "2.12.20"
def Scala213 = "2.13.16"
def Scala3 = "3.7.2"

val tagName = Def.setting {
  s"v${if (releaseUseGlobalVersion.value) (ThisBuild / version).value else version.value}"
}

val tagOrHash = Def.setting {
  if (isSnapshot.value) sys.process.Process("git rev-parse HEAD").lineStream_!.head
  else tagName.value
}

val commonSettings = Def.settings(
  startYear := Some(2022),
  organization := "com.github.xuwei-k",
  homepage := Some(url("https://github.com/xuwei-k/sbt-warning-diff")),
  licenses := Seq("MIT License" -> url("https://www.opensource.org/licenses/mit-license")),
  Compile / doc / scalacOptions ++= {
    Seq(
      "-sourcepath",
      (LocalRootProject / baseDirectory).value.getAbsolutePath,
      "-doc-source-url",
      s"https://github.com/xuwei-k/sbt-warning-diff/tree/${tagOrHash.value}€{FILE_PATH}.scala"
    )
  },
  pomExtra :=
    <developers>
    <developer>
      <id>xuwei-k</id>
      <name>Kenji Yoshida</name>
      <url>https://github.com/xuwei-k</url>
    </developer>
  </developers>
  <scm>
    <url>git@github.com:xuwei-k/sbt-warning-diff.git</url>
    <connection>scm:git:git@github.com:xuwei-k/sbt-warning-diff.git</connection>
    <tag>{tagOrHash.value}</tag>
  </scm>,
  scalacOptions ++= {
    scalaBinaryVersion.value match {
      case "3" =>
        Nil
      case "2.13" =>
        Seq("-Xsource:3-cross")
      case _ =>
        Seq("-Xsource:3")
    }
  },
  scalacOptions ++= Seq(
    "-deprecation",
    "-unchecked",
    "-Xlint",
    "-language:existentials",
    "-language:higherKinds",
    "-language:implicitConversions"
  ),
  releaseTagName := tagName.value,
  releaseProcess := Seq[ReleaseStep](
    checkSnapshotDependencies,
    inquireVersions,
    runClean,
    setReleaseVersion,
    commitReleaseVersion,
    tagRelease,
    releaseStepCommandAndRemaining("publishSigned"),
    releaseStepCommandAndRemaining("sonaRelease"),
    setNextVersion,
    commitNextVersion,
    pushChanges
  ),
  publishTo := (if (isSnapshot.value) None else localStaging.value)
)

val sbtVersionForCross = Def.setting(
  scalaBinaryVersion.value match {
    case "2.12" =>
      sbtVersion.value
    case _ =>
      "2.0.0-RC3"
  }
)

val pluginSettings = Def.settings(
  commonSettings,
  pluginCrossBuild / sbtVersion := sbtVersionForCross.value,
  scriptedBufferLog := false,
  scriptedLaunchOpts ++= {
    val javaVmArgs = {
      import scala.collection.JavaConverters.*
      java.lang.management.ManagementFactory.getRuntimeMXBean.getInputArguments.asScala.toList
    }
    javaVmArgs.filter(a => Seq("-Xmx", "-Xms", "-XX", "-Dsbt.log.noformat").exists(a.startsWith))
  },
  scriptedLaunchOpts ++= Seq(
    "-Dxuwei.scalafix-rules.version=" + xuweiScalafixRules.revision,
    "-Dplugin.version=" + version.value
  )
)

val core = projectMatrix
  .settings(
    commonSettings,
    name := "warning-diff-core",
    libraryDependencies += "org.scala-sbt" % "util-interface" % sbtVersionForCross.value,
    libraryDependencies += {
      // Don't update. use same version as sbt
      scalaBinaryVersion.value match {
        case "3" =>
          "com.eed3si9n" %% "sjson-new-scalajson" % "0.14.0-M5"
        case _ =>
          "com.eed3si9n" %% "sjson-new-scalajson" % "0.9.1" // scala-steward:off
      }
    },
    buildInfoKeys := Seq[BuildInfoKey](version),
    buildInfoObject := "WarningDiffBuildInfo",
    buildInfoPackage := "warning_diff"
  )
  .defaultAxes(VirtualAxis.jvm)
  .enablePlugins(BuildInfoPlugin)
  .jvmPlatform(
    Seq(Scala212, Scala213, Scala3)
  )

val plugin = projectMatrix
  .in(file("sbt-warning-diff"))
  .jvmPlatform(
    Seq(Scala212, Scala3)
  )
  .enablePlugins(SbtPlugin)
  .settings(
    pluginSettings,
    name := "sbt-warning-diff"
  )
  .dependsOn(
    core
  )

val scalafixPlugin = project
  .in(file("scalafix"))
  .enablePlugins(SbtPlugin)
  .settings(
    pluginSettings,
    addSbtPlugin("ch.epfl.scala" % "sbt-scalafix" % "0.14.3"),
    name := "warning-diff-scalafix-plugin"
  )
  .dependsOn(plugin.jvm(Scala212))

val fix = projectMatrix
  .in(file("fix"))
  .settings(
    commonSettings,
    name := "warning-diff-scalafix",
    libraryDependencies += "org.scala-sbt" %% "io" % "1.10.5",
    libraryDependencies += "ch.epfl.scala" %% "scalafix-core" % "0.14.3"
  )
  .dependsOn(core)
  .defaultAxes(VirtualAxis.jvm)
  .jvmPlatform(
    Seq(Scala212, Scala213)
  )

commonSettings
publish / skip := true

inThisBuild(
  List(
    semanticdbEnabled := true,
    semanticdbVersion := _root_.scalafix.sbt.BuildInfo.scalametaVersion,
    scalafixOnCompile := {
      sys.env.isDefinedAt("GITHUB_ACTION") == false
    }
  )
)

lazy val xuweiScalafixRules = "com.github.xuwei-k" %% "scalafix-rules" % "0.6.14"

ThisBuild / scalafixDependencies += xuweiScalafixRules
