package warning_diff

import com.typesafe.config.ConfigFactory
import java.io.File
import sbt.io.IO
import scala.jdk.CollectionConverters.*
import scala.meta.inputs.Input
import scalafix.lint.RuleDiagnostic
import scalafix.v1.SyntacticDocument
import scalafix.v1.SyntacticRule
import sjsonnew.BasicJsonProtocol.*
import sjsonnew.JsonReader
import warning_diff.JsonClassOps.*

object ScalafixWarning {
  private case class Result(
    input: Input.VirtualFile,
    diagnostic: RuleDiagnostic
  )

  def main(args: Array[String]): Unit = {
    val unbuilder = new sjsonnew.Unbuilder(sjsonnew.support.scalajson.unsafe.Converter.facade)
    val json = sjsonnew.support.scalajson.unsafe.Parser.parseFromFile(new File("input.json")).get
    val in = implicitly[JsonReader[FixInput]].read(Some(json), unbuilder)
    val base = new File(in.base)
    val result = in.projects.map { proj =>
      val confRules = ConfigFactory.parseString(proj.scalafixConfig).getStringList("rules").asScala.toSet
      val allRules = scalafix.internal.v1.Rules.all()
      val runRules = allRules
        .collect {
          case x if confRules(x.name.value) =>
            x.withConfiguration(
              scalafix.v1.Configuration
                .apply()
                .withConf(
                  metaconfig.Conf.parseString(proj.scalafixConfig)(metaconfig.Hocon).get
                )
                .withScalacOptions(proj.scalacOptions.toList)
                .withScalaVersion(proj.scalaVersion)
            ).get
        }
        .collect {
          case x: SyntacticRule =>
            // only support SyntacticRule
            x
        }
      val sourceFileNames = proj.sources
      val diagnostics = sourceFileNames.flatMap { sourceFileName =>
        val src = IO.read(new File(sourceFileName))
        val input = scala.meta.Input.VirtualFile(
          "${BASE}/" + IO.relativize(base, new File(sourceFileName)).getOrElse(sys.error(s"${base} ${sourceFileName}")),
          src
        )
        val parse = implicitly[scala.meta.parsers.Parse[scala.meta.Source]]
        val tree = parse.apply(input = input, dialect = convertDialect(proj.dialect)).get
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

      FixOutput(
        projectId = proj.projectId,
        sbtConfig = proj.sbtConfig,
        warnings = diagnostics.map { x =>
          warning_diff.Warning(
            message = s"[${x.diagnostic.id.fullID}] ${x.diagnostic.message}",
            position = convertPosition(x.input, x.diagnostic.position)
          )
        }
      )
    }

    IO.write(
      new File(in.output),
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
