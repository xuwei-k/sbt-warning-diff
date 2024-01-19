package warning_diff

import sjsonnew.BasicJsonProtocol._
import warning_diff.JsonClassOps._
import java.io.File
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import scala.jdk.CollectionConverters._
import scalafix.internal.config.ScalaVersion
import scalafix.v1.SyntacticDocument
import scalafix.v1.SyntacticRule
import com.typesafe.config.ConfigFactory
import scala.meta.inputs.Input

object ScalafixWarning {
  def main(args: Array[String]): Unit = {
    val confRules = ConfigFactory.parseFile(new File(".scalafix.conf")).getStringList("rules").asScala.toSet
    val allRules = java.util.ServiceLoader.load(classOf[scalafix.v1.Rule])
    val syntactics = allRules.iterator().asScala.collect { case x: SyntacticRule => x }.toList
    val runRules = syntactics.filter(x => confRules(x.name.value))
    val sourceFileNames =
      scala.io.Source.fromInputStream(this.getClass.getResourceAsStream("/source-files.txt")).getLines().toList

    println(runRules)

    val diagnostics = sourceFileNames.flatMap { sourceFileName =>
      val src = new String(Files.readAllBytes(new File(sourceFileName).toPath), StandardCharsets.UTF_8)
      val input = scala.meta.Input.VirtualFile(sourceFileName, src)
      val doc = SyntacticDocument.fromInput(input, ScalaVersion.scala3)
      val map = runRules.map(rule => rule.name -> rule.fix(doc)).toMap
      scalafix.internal.patch.PatchInternals
        .syntactic(
          map,
          doc,
          false
        )
        .diagnostics
    }
    println(diagnostics.size)
    diagnostics.foreach(println)
    val result = diagnostics
      .map(_.diagnostic)
      .map { x =>
        warning_diff.Warning(
          message = x.message,
          position = convertPosition(x.position)
        )
      }

    Files
      .write(
        new File("target", "scalafix-result.txt").toPath,
        result.toJsonString.getBytes(StandardCharsets.UTF_8)
      )
  }

  private def convertPosition(p: scala.meta.Position): Pos = {
    Pos(
      line = Some(p.startLine),
      lineContent = p.text,
      offset = None,
      pointer = None,
      pointerSpace = None,
      sourcePath = Option(p.input).collect { case x: Input.VirtualFile => x.path },
      startOffset = None,
      endOffset = None,
      startLine = Some(p.startLine),
      startColumn = Some(p.startColumn),
      endLine = Some(p.endLine),
      endColumn = Some(p.endColumn)
    )
  }
}
