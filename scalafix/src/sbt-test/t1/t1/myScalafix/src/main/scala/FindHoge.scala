package fix

import scala.meta.Term
import scalafix.Patch
import scalafix.lint.Diagnostic
import scalafix.lint.LintSeverity
import scalafix.v1.SyntacticDocument
import scalafix.v1.SyntacticRule

class FindHoge extends SyntacticRule("FindHoge") {
  override def fix(implicit doc: SyntacticDocument): Patch = {
    doc.tree.collect { case t @ Term.Name("hoge") =>
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

