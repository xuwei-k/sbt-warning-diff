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
    ScalafixConfig / products += Def.uncached {
      fileConverter.value.toPath((LocalProject("myScalafix") / Compile / packageBin).value).toFile
    },
    ScalafixConfig / products ++= Def.uncached {
      (LocalProject("myScalafix") / Compile / externalDependencyClasspath).value.map(x => fileConverter.value.toPath(x.data).toFile)
    }
  )
}
