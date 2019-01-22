package fix

import scalafix.v1._

import scala.meta._

/**
  * Example scalafix rule that replaces the unit return type with IO[Unit].
  * This is just an example and will likely have a few bugs. Please don't use this in production.
  *
  * This is a semantic rule, because it needs to access SemanticDB to understand symbol information
  */
class Noreturnunit extends SemanticRule("Noreturnunit") {

  override def fix(implicit doc: SemanticDocument): Patch =
  /**
    * This is the main method of the rule. We start with the document tree (everything that's in a file)
    * and we match on all subtrees using .collect
    */
    doc.tree.collect {
      case t: Decl if getType(t.symbol).map(_.toString).contains("Unit") =>
        Patch.lint(Diagnostic("ReturnError", "Return type is not allowed", t.pos))
      case t: Defn if getType(t.symbol).map(_.toString).contains("Unit") && !isMain(t) =>
        Patch.lint(Diagnostic("ReturnError", "Return type is not allowed", t.pos))
    }.asPatch

  /**
    * Utility to understand if is main. Note that we use quasiquotes for pattern matching
    */
  def isMain(d: Defn): Boolean =
    d match {
      case q"def main(args: Array[String]): Unit = $body" =>
        true
      case _ => false
    }
  
  /**
    * Utility to extract type from a symbol by using SemanticDB
    */
  def getType(symbol: Symbol)(implicit doc: SemanticDocument): Option[SemanticType] =
    symbol.info.get.signature match {
      case MethodSignature(_, _, returnType) =>
        Some(returnType)
      case ValueSignature(t) =>
        Some(t)
      case _ => None
    }
}
