package warning_diff

import sbt.Keys._
import sbt._
import scalafix.sbt.ScalafixPlugin
import warning_diff.WarningDiffPlugin.autoImport._

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
      (x / warnings) := {
        Nil
      }
    )
  }

}
