package warning_diff

import sbt.Keys._
import sbt._
import scalafix.sbt.ScalafixPlugin
import scalafix.sbt.ScalafixPlugin.autoImport.scalafixDependencies
import sjsonnew.JsonReader
import warning_diff.JsonClassOps._
import warning_diff.WarningDiffPlugin.Warnings
import warning_diff.WarningDiffPlugin.autoImport._
import java.io.File
import sjsonnew.BasicJsonProtocol._

object WarningDiffScalafixPlugin extends AutoPlugin {
  override def requires: Plugins = WarningDiffPlugin && ScalafixPlugin

  override def trigger: PluginTrigger = allRequirements

  object autoImport {
    val warningsScalafixFiles = taskKey[Seq[File]]("")
  }

  import autoImport._

  override def projectSettings: Seq[Def.Setting[?]] = WarningDiffPlugin.warningConfigs.flatMap { x =>
    Def.settings(
      (x / warningsScalafixFiles) := {
        (x / unmanagedSources).value.filter(_.getName.endsWith(".scala"))
      },
      (x / warnings) ++= {
        val src = (x / warningsScalafixFiles).value.map(_.getCanonicalPath)
        val launcher = sbtLauncher.value
        IO.withTemporaryDirectory { tmp =>
          val input = FixInput(
            scalafixConfig = IO.read(
              _root_.scalafix.sbt.ScalafixPlugin.autoImport.scalafixConfig.value
                .getOrElse(
                  file(".scalafix.conf")
                )
            ),
            sources = src,
            dependencies = (ThisBuild / scalafixDependencies).value.map(x =>
              Dependency(
                groupId = x.organization,
                artifactId = x.name,
                version = x.revision
              )
            ),
            output = tmp.getCanonicalPath
          )
          val buildSbt = Seq[String](
            """scalaVersion := "2.13.12" """,
            """libraryDependencies += "com.github.xuwei-k" %% "warning-diff-scalafix" % "0.1.2-SNAPSHOT" """,
            (ThisBuild / scalafixDependencies).value
              .map(x => s"""  "${x.organization}" %% "${x.name}" % "${x.revision}",""")
              .mkString("libraryDependencies ++= Seq(\n", "\n", "\n)")
          ).mkString("\n\n")

          println(buildSbt)

          IO.write(tmp / "build.sbt", buildSbt)
          IO.write(tmp / "input.json", input.toJsonString)
          val exitCode = Fork.java.apply(
            ForkOptions().withWorkingDirectory(tmp),
            Seq(
              "-jar",
              launcher.getCanonicalPath,
              "runMain warning_diff.ScalafixWarning"
            )
          )
          assert(exitCode == 0, s"exit code = $exitCode")
          val unbuilder = new sjsonnew.Unbuilder(sjsonnew.support.scalajson.unsafe.Converter.facade)
          val json = {
            val output = IO.read(tmp / "output.json")
            sjsonnew.support.scalajson.unsafe.Parser.parseUnsafe(output)
          }
          implicitly[JsonReader[Warnings]].read(Some(json), unbuilder)
        }
      }
    )
  }

  private[this] def getJarFiles(module: ModuleID): Def.Initialize[Task[Seq[File]]] = Def.task {
    dependencyResolution.value
      .retrieve(
        dependencyId = module,
        scalaModuleInfo = scalaModuleInfo.value,
        retrieveDirectory = csrCacheDirectory.value,
        log = streams.value.log
      )
      .left
      .map(e => throw e.resolveException)
      .merge
      .distinct
  }

  private[this] def sbtLauncher: Def.Initialize[Task[File]] = Def.taskDyn {
    val v = sbtVersion.value
    Def.task {
      val Seq(launcher) = getJarFiles("org.scala-sbt" % "sbt-launch" % v).value
      launcher
    }
  }
}
