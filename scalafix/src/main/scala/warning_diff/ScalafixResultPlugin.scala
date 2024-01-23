package warning_diff

import sbt.*
import scalafix.interfaces.ScalafixArguments
import scalafix.interfaces.ScalafixDiagnostic
import scalafix.internal.sbt.Arg
import scalafix.internal.sbt.ScalafixInterface
import scalafix.sbt.ScalafixPlugin

object ScalafixResultPlugin extends AutoPlugin {
  override def requires: Plugins = ScalafixPlugin

  override def trigger: PluginTrigger = allRequirements

  object autoImport {
    val scalafixResult = taskKey[List[ScalafixDiagnostic]]("")
  }

  private[this] val scalafixBuffer =
    settingKey[scala.collection.mutable.Buffer[ScalafixDiagnostic]]("")

  private[this] val bufferLock = new Object

  import autoImport.*

  private[this] val scalafixInterfaceProvider: SettingKey[() => ScalafixInterface] = {
    val method = ScalafixPlugin.getClass.getDeclaredMethods
      .find(_.getName.contains("scalafixInterfaceProvider"))
      .get
    method.setAccessible(true)
    method.invoke(ScalafixPlugin).asInstanceOf[SettingKey[() => ScalafixInterface]]
  }

  private[this] def modifyScalafixInterface(
    x: ScalafixInterface,
    f: (ScalafixArguments, Seq[Arg]) => (ScalafixArguments, Seq[Arg])
  ): ScalafixInterface = {
    val clazz = classOf[ScalafixInterface]
    val field = clazz.getDeclaredField("scalafixArguments")
    field.setAccessible(true)
    val arguments = field.get(x).asInstanceOf[ScalafixArguments]
    val (a1, a2) = f(arguments, x.args)
    val constructor = clazz.getConstructor(classOf[ScalafixArguments], classOf[Seq[?]])
    constructor.newInstance(a1, a2)
  }

  override def projectSettings: Seq[Def.Setting[?]] = Def.settings(
    scalafixBuffer := scala.collection.mutable.Buffer.empty,
    scalafixResult := Def.taskDyn {
      bufferLock.synchronized {
        scalafixBuffer.value.clear()
      }
      _root_.scalafix.sbt.ScalafixPlugin.autoImport.scalafixAll
        .toTask("")
        .map { _ =>
          bufferLock.synchronized {
            scalafixBuffer.value.toList
          }
        }
    }.value,
    scalafixInterfaceProvider := { () =>
      val old = scalafixInterfaceProvider.value.apply()
      modifyScalafixInterface(
        old,
        (i, args) =>
          (
            i.withMainCallback(diagnostic => {
              bufferLock.synchronized(
                scalafixBuffer.value.append(diagnostic)
              )
            }),
            args
          )
      )
    }
  )
}
