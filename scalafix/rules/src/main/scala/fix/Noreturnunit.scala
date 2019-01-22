package fix

import scalafix.v1._

import scala.meta._

class Noreturnunit extends SemanticRule("Noreturnunit") {

  override def fix(implicit doc: SemanticDocument): Patch = 
    doc.tree.collect {
      case t: Decl if getType(t.symbol).map(_.toString).contains("Unit") =>
        Patch.lint(Diagnostic("ReturnError", "Return type is not allowed", t.pos))
      case t: Defn if getType(t.symbol).map(_.toString).contains("Unit") && !isMain(t) =>
        Patch.lint(Diagnostic("ReturnError", "Return type is not allowed", t.pos))
    }.asPatch

  def isMain(d: Defn): Boolean =
    d match {
      case q"def main(args: Array[String]): Unit = $body" =>
        true
      case _ => false
    }

  def getType(symbol: Symbol)(implicit doc: SemanticDocument): Option[SemanticType] =
    symbol.info.get.signature match {
      case MethodSignature(_, _, returnType) =>
        Some(returnType)
      case ValueSignature(t) =>
        Some(t)
      case _ => None
    }
}
