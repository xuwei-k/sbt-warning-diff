package warning_diff

import com.typesafe.config.ConfigFactory
import java.io.File
import sbt.io.IO
import scala.jdk.CollectionConverters._
import scala.meta.inputs.Input
import scalafix.lint.RuleDiagnostic
import scalafix.v1.SyntacticDocument
import scalafix.v1.SyntacticRule
import sjsonnew.BasicJsonProtocol._
import sjsonnew.JsonReader
import warning_diff.JsonClassOps._

object ScalafixWarning {
  private case class Result(
    input: Input.VirtualFile,
    diagnostic: RuleDiagnostic
  )

  def main(args: Array[String]): Unit = {
    val unbuilder = new sjsonnew.Unbuilder(sjsonnew.support.scalajson.unsafe.Converter.facade)
    val jsonString = IO.read(new File("input.json"))
    val json = sjsonnew.support.scalajson.unsafe.Parser.parseUnsafe(jsonString)
    val in = implicitly[JsonReader[FixInput]].read(Some(json), unbuilder)
    val base = new File(in.base)
    val confRules = ConfigFactory.parseString(in.scalafixConfig).getStringList("rules").asScala.toSet
    val allRules = scalafix.internal.v1.Rules.all()
    val syntactics = allRules.collect { case x: SyntacticRule => x } // only support SyntacticRule
    val runRules = syntactics.filter(x => confRules(x.name.value))
    val sourceFileNames = in.sources
    val diagnostics = sourceFileNames.flatMap { sourceFileName =>
      val src = IO.read(new File(sourceFileName))
      val input = scala.meta.Input.VirtualFile(
        "${BASE}/" + IO.relativize(base, new File(sourceFileName)).getOrElse(sys.error(s"${base} ${sourceFileName}")),
        src
      )
      val parse = implicitly[scala.meta.parsers.Parse[scala.meta.Source]]
      val tree = parse.apply(input = input, dialect = convertDialect(in.dialect)).get
      val doc = SyntacticDocument.fromTree(tree)
      val map = runRules.map(rule => rule.name -> rule.fix(doc)).toMap
      scalafix.internal.patch.PatchInternals
        .syntactic(
          map,
          doc,
          false
        )
        .diagnostics
        .map(x => Result(input = input, diagnostic = x))
    }
    val result = diagnostics
      .map { x =>
        warning_diff.Warning(
          message = s"[${x.diagnostic.id.fullID}] ${x.diagnostic.message}",
          position = convertPosition(x.input, x.diagnostic.position)
        )
      }

    IO.write(
      new File(new File(in.output), "output.json"),
      result.toJsonString
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

  private def convertDialect(x: warning_diff.Dialect): scala.meta.Dialect = x match {
    case Dialect.Scala210 =>
      scala.meta.dialects.Scala210
    case Dialect.Scala211 =>
      scala.meta.dialects.Scala211
    case Dialect.Scala212 =>
      scala.meta.dialects.Scala212
    case Dialect.Scala213 =>
      scala.meta.dialects.Scala213
    case Dialect.Scala212Source3 =>
      scala.meta.dialects.Scala212Source3
    case Dialect.Scala213Source3 =>
      scala.meta.dialects.Scala213Source3
    case Dialect.Scala3 =>
      scala.meta.dialects.Scala3
  }
}
