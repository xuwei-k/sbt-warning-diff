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
    val warningsScalafix = taskKey[Warnings]("")
  }

  import autoImport._

  private def moduleIdToString(m: ModuleID): String = {
    def q(s: String): String = "\"" + s + "\""
    m.crossVersion match {
      case _: CrossVersion.Binary =>
        s"""${q(m.organization)} %% ${q(m.name)} % ${q(m.revision)}"""
      case _: CrossVersion.Full =>
        s"""${q(m.organization)} % ${q(m.name)} % ${q(m.revision)} cross CrossVersion.full"""
      case _ =>
        s"""${q(m.organization)} % ${q(m.name)} % ${q(m.revision)}"""
    }
  }

  override def projectSettings: Seq[Def.Setting[?]] = Def.settings(
    ThisBuild / warningsScalafix := {
      val src: Seq[File] = Def.taskDyn {
        val extracted = Project.extract(state.value)
        val currentBuildUri = extracted.currentRef.build
        extracted.structure.units
          .apply(currentBuildUri)
          .defined
          .values
          .filter(
            _.autoPlugins.contains(WarningDiffScalafixPlugin)
          )
          .toList
          .flatMap { p =>
            WarningDiffPlugin.warningConfigs.map { x =>
              LocalProject(p.id) / x / warningsScalafixFiles
            }
          }
          .join
          .map(_.flatten)
      }.value

      val launcher = sbtLauncher.value
      if (src.nonEmpty) {
        val deps = (ThisBuild / scalafixDependencies).value ++ Seq(
          "ch.epfl.scala" %% "scalafix-rules" % _root_.scalafix.sbt.BuildInfo.scalafixVersion,
          "com.github.xuwei-k" %% "warning-diff-scalafix" % WarningDiffBuildInfo.version
        )
        IO.withTemporaryDirectory { tmp =>
          val input = FixInput(
            scalafixConfig = IO.read(
              _root_.scalafix.sbt.ScalafixPlugin.autoImport.scalafixConfig.value
                .getOrElse(
                  file(".scalafix.conf")
                )
            ),
            sources = src.map(_.getCanonicalPath),
            base = (LocalRootProject / baseDirectory).value.getCanonicalPath,
            output = tmp.getCanonicalPath
          )

          val buildSbt = Seq[String](
            """scalaVersion := "2.13.12" """,
            deps
              .map(moduleIdToString)
              .mkString("libraryDependencies ++= Seq(\n", ",\n", "\n)")
          ).mkString("\n\n")

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
      } else {
        Nil
      }
    },
    WarningDiffPlugin.warningConfigs.flatMap { x =>
      Def.settings(
        (x / warningsScalafixFiles) := {
          (x / unmanagedSources).value.filter(_.getName.endsWith(".scala"))
        },
        (x / warnings) ++= {
          val all = (ThisBuild / warningsScalafix).value
          val base = (LocalRootProject / baseDirectory).value
          val files = (x / warningsScalafixFiles).value.flatMap { x =>
            IO.relativize(base = base, file = x).map("${BASE}/" + _)
          }.toSet
          all.map(x => (x, x.position.sourcePath)).collect {
            case (x, Some(path)) if files(path) => x
          }
        }
      )
    }
  )

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
