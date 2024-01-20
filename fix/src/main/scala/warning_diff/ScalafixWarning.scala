package warning_diff

import sbt.io.IO
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
import sjsonnew.JsonReader
import scala.meta.inputs.Input

object ScalafixWarning {
  def main(args: Array[String]): Unit = {
    val unbuilder = new sjsonnew.Unbuilder(sjsonnew.support.scalajson.unsafe.Converter.facade)
    val jsonString = Files.readAllLines(new File("input.json").toPath).asScala.mkString("\n")
    println("input = " + jsonString)
    val json = sjsonnew.support.scalajson.unsafe.Parser.parseUnsafe(jsonString)
    val input = implicitly[JsonReader[FixInput]].read(Some(json), unbuilder)
    val base = new File(input.base)
    val confRules = ConfigFactory.parseString(input.scalafixConfig).getStringList("rules").asScala.toSet
    val allRules = scalafix.internal.v1.Rules.all()
    val syntactics = allRules.collect { case x: SyntacticRule => x }
    val runRules = syntactics.filter(x => confRules(x.name.value))
    val sourceFileNames = input.sources
    println("run rules = " + runRules)
    val diagnostics = sourceFileNames.flatMap { sourceFileName =>
      val src = new String(Files.readAllBytes(new File(sourceFileName).toPath), StandardCharsets.UTF_8)
      println("src = " + src)
      val input = scala.meta.Input.VirtualFile(
        "${BASE}/" + IO.relativize(base, new File(sourceFileName)).getOrElse(sys.error(s"${base} ${sourceFileName}")),
        src
      )
      val doc = SyntacticDocument.fromInput(input, ScalaVersion.scala3)
      val map = runRules.map(rule => rule.name -> rule.fix(doc)).toMap
      val xxx = scalafix.internal.patch.PatchInternals
        .syntactic(
          map,
          doc,
          false
        )
      println(xxx)
      xxx.diagnostics.map(input -> _)
    }
    println(diagnostics.size)
    diagnostics.foreach(println)
    val result = diagnostics
      .map { x =>
        warning_diff.Warning(
          message = x._2.message,
          position = convertPosition(x._1, x._2.position)
        )
      }

    Files
      .write(
        new File(new File(input.output), "output.json").toPath,
        result.toJsonString.getBytes(StandardCharsets.UTF_8)
      )
  }

  private def convertPosition(input: Input.VirtualFile, p: scala.meta.Position): Pos = {
    Pos(
      line = Some(p.startLine),
      lineContent = input.value.linesIterator.drop(p.startLine).next(),
      offset = None,
      pointer = None,
      pointerSpace = None, // TODO
      sourcePath = Some(input.path),
      startOffset = None,
      endOffset = None,
      startLine = Some(p.startLine),
      startColumn = Some(p.startColumn),
      endLine = Some(p.endLine),
      endColumn = Some(p.endColumn)
    )
  }
}
