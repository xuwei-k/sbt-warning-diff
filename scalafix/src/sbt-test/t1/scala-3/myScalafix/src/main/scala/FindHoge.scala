package fix

import scala.meta.Term
import scalafix.lint.LintSeverity
import scalafix.v1.*

class FindHoge extends SyntacticRule("FindHoge") {

  override def fix(implicit doc: SyntacticDocument): Patch = {
    doc.tree.collect {
      case t @ Term.Name("hoge") =>
        Patch.lint(
          Diagnostic(
            id = "",
            message = "aaa",
            position = t.pos,
            severity = LintSeverity.Warning
          )
        )
    }.asPatch
  }
}
