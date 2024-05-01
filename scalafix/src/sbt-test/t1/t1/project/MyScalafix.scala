import sbt.*
import sbt.Keys.*
import scalafix.sbt.ScalafixPlugin
import scalafix.sbt.ScalafixPlugin.autoImport.ScalafixConfig

object MyScalafix extends AutoPlugin {

  override def trigger: PluginTrigger =
    allRequirements

  override def requires: Plugins =
    ScalafixPlugin

  override def projectSettings: Seq[Def.Setting[?]] = Def.settings(
    ScalafixConfig / products += {
      scalaBinaryVersion.value match {
        case "2.12" =>
          (LocalProject("myScalafix2_12") / Compile / packageBin).value
        case _ =>
          (LocalProject("myScalafix2_13") / Compile / packageBin).value
      }
    },
    ScalafixConfig / products ++= {
      scalaBinaryVersion.value match {
        case "2.12" =>
          (LocalProject("myScalafix2_12") / Compile / externalDependencyClasspath).value.map(_.data)
        case _ =>
          (LocalProject("myScalafix2_13") / Compile / externalDependencyClasspath).value.map(_.data)
      }
    }
  )
}
