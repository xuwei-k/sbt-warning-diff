import sbt._
import sbt.Keys._
import scalafix.sbt.ScalafixPlugin
import scalafix.sbt.ScalafixPlugin.autoImport.ScalafixConfig

object MyScalafix extends AutoPlugin {

  override def trigger: PluginTrigger =
    allRequirements

  override def requires: Plugins =
    ScalafixPlugin

  override def projectSettings: Seq[Def.Setting[?]] = Def.settings(
    ScalafixConfig / products += {
      (LocalProject("myScalafix") / Compile / packageBin).value
    },
    ScalafixConfig / products ++= {
      (LocalProject("myScalafix") / Compile / externalDependencyClasspath).value.map(_.data)
    }
  )
}
