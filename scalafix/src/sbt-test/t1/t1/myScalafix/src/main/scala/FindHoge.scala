package fix

import metaconfig.Configured
import scala.meta.Term
import scalafix.Patch
import scalafix.lint.Diagnostic
import scalafix.lint.LintSeverity
import scalafix.v1.Configuration
import scalafix.v1.Rule
import scalafix.v1.SyntacticDocument
import scalafix.v1.SyntacticRule

class FindHoge(myConfig: String) extends SyntacticRule("FindHoge") {
  def this() = this("default value")

  override def withConfiguration(config: Configuration): Configured[Rule] = {
    assert(config.scalacOptions == List("-deprecation"), config.scalacOptions)
    assert(config.scalaVersion.nonEmpty)
    config.conf.get[String]("FindHoge").map(x => new FindHoge(x))
  }

  override def fix(implicit doc: SyntacticDocument): Patch = {
    assert(myConfig == "aaaaa", myConfig)
    doc.tree.collect {
      case t @ Term.Name("hoge") =>
        Patch.lint(
          Diagnostic(
            id = "",
            message = "hogehoge",
            position = t.pos,
            severity = LintSeverity.Warning
          )
        )
    }.asPatch
  }
}
