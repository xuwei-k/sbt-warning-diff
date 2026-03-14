package warning_diff

import java.io.File
import metaconfig.ConfDecoder
import metaconfig.generic.Surface
import sbt.io.IO
import scala.meta.inputs.Input
import scalafix.lint.LintSeverity
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
    try {
      run()
    } catch {
      case e: LinkageError =>
        // https://github.com/sbt/sbt/issues/7408
        throw new RuntimeException(e)
    }
  }

  /**
    * @param dialectOverride [[https://github.com/scalacenter/scalafix/commit/2529c4d42ef25511c6576d17c1cc287a5515d9d2]]
    */
  private final case class ScalafixConfig(
    rules: Seq[String],
    dialectOverride: Map[String, Boolean]
  )

  private object ScalafixConfig {
    val default = ScalafixConfig(
      rules = Nil,
      dialectOverride = Map.empty
    )
    implicit val surface: Surface[ScalafixConfig] =
      metaconfig.generic.deriveSurface[ScalafixConfig]
    implicit val decoder: ConfDecoder[ScalafixConfig] =
      metaconfig.generic.deriveDecoder(default)
  }

  private def run(): Unit = {
    val unbuilder = new sjsonnew.Unbuilder(sjsonnew.support.scalajson.unsafe.Converter.facade)
    val json = sjsonnew.support.scalajson.unsafe.Parser.parseFromFile(new File("input.json")).get
    val in = implicitly[JsonReader[FixInput]].read(Some(json), unbuilder)
    val base = new File(in.base)
    val result = in.projects.map { proj =>
      val config = metaconfig.Conf.parseString(proj.scalafixConfig)(metaconfig.Hocon).get.as[ScalafixConfig].get
      val confRules = config.rules.toSet
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
        val dialect = convertDialect(proj.dialect, config.dialectOverride)
        val tree = parse.apply(input = input, dialect = dialect).get
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
            position = convertPosition(x.input, x.diagnostic.position),
            severity = {
              x.diagnostic.severity match {
                case LintSeverity.Warning =>
                  None
                case LintSeverity.Info =>
                  Some("INFO")
                case LintSeverity.Error =>
                  Some("ERROR")
              }
            }
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
      line = Some(p.startLine + 1),
      lineContent = input.value.linesIterator.drop(p.startLine).next(),
      offset = None,
      pointer = None,
      pointerSpace = None,
      sourcePath = Some(input.path),
      startOffset = None,
      endOffset = None,
      startLine = Some(p.startLine + 1),
      startColumn = Some(p.startColumn),
      endLine = Some(p.endLine + 1),
      endColumn = Some(p.endColumn)
    )
  }

  private def convertDialect(x: warning_diff.Dialect, dialectOverride: Map[String, Boolean]): scala.meta.Dialect = {
    val value = x match {
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

    // https://github.com/scalacenter/scalafix/commit/2529c4d42ef25511c6576d17c1cc287a5515d9d2
    dialectOverride.foldLeft(value) {
      case (cur, (k, v)) if k.nonEmpty =>
        val upper = s"${k.head.toUpper}${k.drop(1)}"
        cur.getClass.getMethods
          .find(method =>
            (
              method.getName == s"with${upper}"
            ) && (
              method.getParameterTypes.toSeq == Seq(classOf[Boolean])
            ) && (
              method.getReturnType == classOf[scala.meta.Dialect]
            )
          )
          .fold(cur)(
            _.invoke(cur, java.lang.Boolean.valueOf(v)).asInstanceOf[scala.meta.Dialect]
          )
      case (cur, _) =>
        cur
    }
  }
}
