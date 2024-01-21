package warning_diff

import sbt.Keys.*
import sbt.*
import sbt.internal.inc.Analysis
import sbt.plugins.JvmPlugin
import sjsonnew.BasicJsonProtocol.*
import sjsonnew.JsonFormat
import sjsonnew.Unbuilder
import warning_diff.JsonClassOps.*
import xsbti.Severity

object WarningDiffPlugin extends AutoPlugin {
  object autoImport {
    val warningsDiffFile = settingKey[File]("")
    val warningsCurrentFile = settingKey[File]("")
    val warningsPreviousFile = settingKey[File]("")
    val warnings = taskKey[Warnings]("")
    val warningsDiff = taskKey[WarningDiff]("")
    val warningsAll = taskKey[Warnings]("")
    val warningsPrevious = taskKey[Option[Warnings]]("")
  }

  import autoImport.*

  type WarningDiff = List[String]
  type Warnings = Seq[Warning]
  def loadWarningsFromJsonFile(file: File): Warnings = {
    val unbuilder = new Unbuilder(sjsonnew.support.scalajson.unsafe.Converter.facade)
    val json = sjsonnew.support.scalajson.unsafe.Parser.parseFromFile(file).get
    implicitly[JsonFormat[Warnings]].read(Option(json), unbuilder)
  }

  override def trigger = allRequirements

  override def requires: Plugins = JvmPlugin

  private[warning_diff] val warningConfigs = Seq(Compile, Test)

  override def projectSettings: Seq[Def.Setting[?]] = warningConfigs.flatMap { x =>
    (x / warnings) := {
      val analysis = (x / Keys.compile).value match {
        case a: Analysis => a
      }
      val problems = analysis.infos.allInfos.values.flatMap(i => i.getReportedProblems ++ i.getUnreportedProblems)
      problems.collect {
        case p if p.severity() == Severity.Warn =>
          Warning.fromSbt(p)
      }.toSeq
    }
  }

  private[this] def dir = "warnings"

  override def buildSettings: Seq[Def.Setting[?]] = Def.settings(
    LocalRootProject / warningsCurrentFile := {
      (LocalRootProject / target).value / dir / "warnings.json"
    },
    LocalRootProject / warningsDiffFile := {
      (LocalRootProject / target).value / dir / "warnings.diff"
    },
    LocalRootProject / warningsPreviousFile := {
      (LocalRootProject / target).value / dir / "warnings-previous.json"
    },
    LocalRootProject / warningsPrevious := {
      val f = (LocalRootProject / warningsPreviousFile).value
      val s = streams.value
      if (f.isFile) {
        Some(loadWarningsFromJsonFile(f))
      } else {
        s.log.warn(s"$f does not exists")
        None
      }
    },
    LocalRootProject / warningsDiff := Def.taskDyn {
      (LocalRootProject / warningsPrevious).?.value.flatten match {
        case Some(previous) =>
          Def.task[WarningDiff] {
            val current = (LocalRootProject / warningsAll).value
            val order: Ordering[Warning] = Ordering.by(x => (x.position.sourcePath, x.position.line, x.message))
            def format(warnings: Warnings): Seq[String] = {
              warnings
                .sorted(order)
                .flatMap(a => List(a.position.sourcePath.getOrElse(""), a.position.lineContent, a.message))
            }

            val result = IO.withTemporaryDirectory { dir =>
              val c = dir / "current.txt"
              val p = dir / "previous.txt"
              IO.writeLines(c, format(current))
              IO.writeLines(p, format(previous))
              sys.process
                .Process(
                  command = Seq("diff", p.getAbsolutePath, c.getAbsolutePath),
                  cwd = Some(dir)
                )
                .lineStream_!
                .toList
            }

            (LocalRootProject / warningsDiffFile).?.value match {
              case Some(diffFile) =>
                streams.value.log.info(s"write to ${diffFile}")
                IO.writeLines(diffFile, result)
              case _ =>
                streams.value.log.warn(s"${warningsDiffFile.key.label} undefined")
            }
            result
          }
        case None =>
          val s = streams.value
          Def.task[WarningDiff] {
            s.log.warn(s"empty ${warningsPrevious.key.label}")
            Nil
          }
      }
    }.value,
    LocalRootProject / warningsAll := {
      val result = Def.taskDyn {
        val extracted = Project.extract(state.value)
        val currentBuildUri = extracted.currentRef.build
        extracted.structure.units
          .apply(currentBuildUri)
          .defined
          .values
          .filter(
            _.autoPlugins.contains(WarningDiffPlugin)
          )
          .toList
          .flatMap { p =>
            warningConfigs.map { x =>
              LocalProject(p.id) / x / warnings
            }
          }
          .join
          .map(_.flatten)
      }.value
      (LocalRootProject / warningsCurrentFile).?.value match {
        case Some(f) =>
          streams.value.log.info(s"write to ${f}")
          IO.write(f, result.toJsonString)
        case None =>
          streams.value.log.warn(s"${warningsDiffFile.key.label} undefined")
      }
      result
    }
  )
}
