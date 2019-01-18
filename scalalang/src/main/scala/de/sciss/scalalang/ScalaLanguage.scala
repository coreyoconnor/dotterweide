package de.sciss.scalalang

import dotterweide.editor.{Adviser, ColorScheme, Coloring}
import dotterweide.formatter.Format
import dotterweide.inspection.Inspection
import dotterweide.lexer.{Lexer, TokenKind}
import dotterweide.parser.Parser
import dotterweide.{Example, FileType, Language}
import de.sciss.scalalang.node.ScalaTokens._

object ScalaLanguage extends Language {
  def name: String = "Scala"

  def description: String = "The Scala programming language"

  def lexer : Lexer   = ScalaLexer
  def parser: Parser  = ScalaParser

  /** A map from color scheme names to the schemes. */
  def colorings: Map[String, Coloring] = Map(
    "Light" -> new ScalaColoring(ColorScheme.LightColors),
    "Dark"  -> new ScalaColoring(ColorScheme.DarkColors))

  /** Pairs of tokens which are symmetric and can be highlighted together,
    * such as matching braces.
    */
  def complements: Seq[(TokenKind, TokenKind)] =
    Seq((LBRACE, RBRACE), (LPAREN, RPAREN), (LBRACKET, RBRACKET))

  /** Default style for formatting the language with white space. */
  def format: Format = ScalaFormat

  /** The syntactic prefix for line comments. */
  def comment: String = "//"

  def inspections: Seq[Inspection] = Nil

  def adviser: Adviser = ScalaAdviser

  def fileType: FileType = FileType("Scala file", "scala")

  def examples: Seq[Example] = ScalaExamples.Values
}