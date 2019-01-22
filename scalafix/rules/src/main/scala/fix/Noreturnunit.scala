package fix

import scalafix.v1._

import scala.meta._

/**
  * Example scalafix rule that replaces the unit return type with IO[Unit].
  * This is just an example and will likely have a few bugs. Please don't use this in production.
  * It's probably a bad idea to replace all Unit values with IO[Unit] blindly.
  *
  * This is a semantic rule, because it needs to access SemanticDB to understand symbol information
  */
class Noreturnunit extends SemanticRule("Noreturnunit") {
  /**
    * This is the main method of the rule. We start with the document tree (everything that's in a file)
    * and we match on all subtrees using .collect
    */
  override def fix(implicit doc: SemanticDocument): Patch = 
    doc.tree.collect {
      /**
        * In the case of declarations we try to match all whose symbol is scala/Unit#
        */
      case t: Decl =>
        t match {
          case d: Decl.Def if unitMatcher.matches(d.decltpe) =>
          // We import cats.effect.IO and we replace the symbol in the declaration with IO[Unit]
          Patch.addGlobalImport(Symbol("cats/effect/IO.")) + Patch.replaceTree(d.decltpe, "IO[Unit]")
          case d: Decl.Val if unitMatcher.matches(d.decltpe) =>
          // Same thing for val
            Patch.addGlobalImport(Symbol("cats/effect/IO.")) + Patch.replaceTree(d.decltpe, "IO[Unit]")
          case _ => Patch.empty
        }
      /**
        * In the case of definitions, since the return type can be omitted we instead use an utility
        * method to extract the return type from semanticdb
        */
      case t: Defn if getType(t.symbol).map(_.toString).contains("Unit") && !isMain(t) =>
        t match {
          case d: Defn.Def  =>
            // We import cats.effect.IO
            Patch.addGlobalImport(Symbol("cats/effect/IO.")) +
            // We replace the type if declared
              d.decltpe.map(t => Patch.replaceTree(t, "IO[Unit]")) +
            // We add an equals symbol if the user is using the deprecated "procedure syntax"
              (if(d.decltpe.exists(_.tokens.isEmpty) && d.body.tokens.head.is[Token.LeftBrace])
                  Patch.addLeft(d.body, "= ")
                else
                  Patch.empty) +
            // If the body starts with curlies, we simply add IO in front, otherwise we wrap the entire body in IO { }
              (if(d.body.tokens.head.is[Token.LeftBrace])
                Patch.replaceTree(d.body, s"IO ${d.body}") else
                Patch.replaceTree(d.body, s"IO { ${d.body} }")
                )
          case d: Defn.Val =>
            // Doing the same for val, except body is now called rhs
            Patch.addGlobalImport(Symbol("cats/effect/IO.")) +
              d.decltpe.map(t => Patch.replaceTree(t, "IO[Unit]")) +
              (if(d.decltpe.exists(_.tokens.isEmpty) && d.rhs.tokens.head.is[Token.LeftBrace])
                Patch.addLeft(d.rhs, "= ")
              else
                Patch.empty) +
              (if(d.rhs.tokens.head.is[Token.LeftBrace])
                Patch.replaceTree(d.rhs, s"IO ${d.rhs}") else
                Patch.replaceTree(d.rhs, s"IO { ${d.rhs} }")
                )
          case _ => Patch.empty
        }
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
    * This matches all scala/Unit# symbols
    */
  val unitMatcher = SymbolMatcher.exact("scala/Unit#")

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
