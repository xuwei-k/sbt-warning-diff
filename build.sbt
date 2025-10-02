import ReleaseTransformations.*

def Scala212 = "2.12.20"
def Scala213 = "2.13.17"
def Scala3 = "3.7.3"

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

def sbt2version = "2.0.0-RC6"

val sbtVersionForCross = Def.setting(
  scalaBinaryVersion.value match {
    case "2.12" =>
      sbtVersion.value
    case _ =>
      sbt2version
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
          "com.eed3si9n" %% "sjson-new-scalajson" % sjsonNewVersion(sbt2version, "3")
        case _ =>
          "com.eed3si9n" %% "sjson-new-scalajson" % sjsonNewVersion(sbtVersion.value, "2.12")
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
    semanticdbVersion := "4.13.10",
    scalafixOnCompile := {
      sys.env.isDefinedAt("GITHUB_ACTION") == false
    }
  )
)

lazy val xuweiScalafixRules = "com.github.xuwei-k" %% "scalafix-rules" % "0.6.16"

ThisBuild / scalafixDependencies += xuweiScalafixRules

def sjsonNewVersion(sbtV: String, scalaBinaryV: String): String = reverseDependencyVersion(
  "org.scala-sbt",
  "sbt",
  sbtV,
  "com.eed3si9n",
  s"sjson-new-scalajson_${scalaBinaryV}"
)

def reverseDependencyVersion(
  baseGroupId: String,
  baseArtifactId: String,
  revision: String,
  targetGroupId: String,
  targetArtifactId: String
): String = {
  import lmcoursier.internal.shaded.coursier
  val dependency = coursier.Dependency(
    coursier.Module(
      coursier.Organization(
        baseGroupId
      ),
      coursier.ModuleName(
        baseArtifactId
      )
    ),
    revision
  )
  coursier.Fetch().addDependencies(dependency).runResult().detailedArtifacts.map(_._1).collect {
    case x if (x.module.organization.value == targetGroupId) && (x.module.name.value == targetArtifactId) => x.version
  } match {
    case Seq(x) =>
      x
    case Nil =>
      sys.error("not found")
    case xs =>
      sys.error(xs.toString)
  }
}
