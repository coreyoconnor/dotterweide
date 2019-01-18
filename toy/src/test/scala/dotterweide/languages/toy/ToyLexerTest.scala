/*
 *  ToyLexerTest.scala
 *  (Dotterweide)
 *
 *  Copyright (c) 2019 the Dotterweide authors. All rights reserved.
 *
 *  This software is published under the GNU Lesser General Public License v2.1+
 *
 *  For further information, please contact Hanns Holger Rutz at
 *  contact@sciss.de
 */

/*
 * Original code copyright 2018 Pavel Fatin, https://pavelfatin.com
 * Licensed under the Apache License, Version 2.0 (the "License"): http://www.apache.org/licenses/LICENSE-2.0
 */

package dotterweide.languages.toy

import org.junit.Test
import org.junit.Assert._

class ToyLexerTest {
  @Test
  def empty(): Unit = {
    assertTokens("", "")
  }

  @Test
  def whitespace(): Unit = {
    assertTokens(" ", "WS( )")
    assertTokens("  ", "WS(  )")
    assertTokens("\t", "WS(\t)")
    assertTokens("\n", "WS(\n)")
    assertTokens("\r\n", "WS(\r\n)")
    assertTokens("\t  \r\n \t ", "WS(\t  \r\n \t )")
  }

  @Test
  def comment(): Unit = {
    assertTokens("//foo", "COMMENT(//foo)")
    assertTokens("// foo", "COMMENT(// foo)")
    assertTokens("///", "COMMENT(///)")
    assertTokens("// // foo", "COMMENT(// // foo)")
    assertTokens("//foo\n", "COMMENT(//foo), WS(\n)")
    assertTokens("//foo\n//bar", "COMMENT(//foo), WS(\n), COMMENT(//bar)")
  }

  @Test
  def number(): Unit = {
    assertTokens("123", "NUMBER_LITERAL(123)")
    assertTokens("1", "NUMBER_LITERAL(1)")
  }

  @Test
  def sequence(): Unit = {
    assertTokens("1 23  4", "NUMBER_LITERAL(1), WS( ), NUMBER_LITERAL(23), WS(  ), NUMBER_LITERAL(4)")
  }

  @Test
  def string(): Unit = {
    assertTokens("\"abc\"", "STRING_LITERAL(\"abc\")")
    assertTokens("\"a\"", "STRING_LITERAL(\"a\")")
    assertTokens("\"a\" \"b\"", "STRING_LITERAL(\"a\"), WS( ), STRING_LITERAL(\"b\")")
    assertTokens("\"123\"", "STRING_LITERAL(\"123\")")
    assertTokens("\"def\"", "STRING_LITERAL(\"def\")")
  }

  @Test
  def unclosedString(): Unit = {
    assertTokens("\"", "error: STRING_LITERAL(\")")
    assertTokens("\"a", "error: STRING_LITERAL(\"a)")
    assertTokens("\"abc", "error: STRING_LITERAL(\"abc)")

    assertTokens("\"\n", "error: STRING_LITERAL(\"), WS(\n)")
    assertTokens("\"abc\n", "error: STRING_LITERAL(\"abc), WS(\n)")

    assertTokens("\"abc\"\"", "STRING_LITERAL(\"abc\"), error: STRING_LITERAL(\")")
    assertTokens("\"a\"\"", "STRING_LITERAL(\"a\"), error: STRING_LITERAL(\")")
    assertTokens("\"\"\"", "STRING_LITERAL(\"\"), error: STRING_LITERAL(\")")
  }

  @Test
  def boolean(): Unit = {
    assertTokens("true", "BOOLEAN_LITERAL(true)")
    assertTokens("false", "BOOLEAN_LITERAL(false)")
  }

  @Test
  def keyword(): Unit = {
    assertTokens("var", "VAR(var)")
    assertTokens("def", "DEF(def)")
    assertTokens("while", "WHILE(while)")
    assertTokens("if", "IF(if)")
    assertTokens("else", "ELSE(else)")
    assertTokens("return", "RETURN(return)")
  }

  @Test
  def keywords(): Unit = {
    assertTokens("def def", "DEF(def), WS( ), DEF(def)")
  }

  @Test
  def types(): Unit = {
    assertTokens("integer", "INTEGER(integer)")
    assertTokens("boolean", "BOOLEAN(boolean)")
    assertTokens("string", "STRING(string)")
    assertTokens("void", "VOID(void)")
  }

  @Test
  def char(): Unit = {
    assertTokens("=", "EQ(=)")
    assertTokens(",", "COMMA(,)")
    assertTokens(":", "COLON(:)")
    assertTokens(";", "SEMI(;)")
    assertTokens("+", "PLUS(+)")
    assertTokens("-", "MINUS(-)")
    assertTokens("*", "STAR(*)")
    assertTokens("/", "SLASH(/)")
    assertTokens("%", "PERCENT(%)")
    assertTokens("!", "BANG(!)")
    assertTokens("(", "LPAREN(()")
    assertTokens(")", "RPAREN())")
    assertTokens("{", "LBRACE({)")
    assertTokens("}", "RBRACE(})")
  }

  @Test
  def chars(): Unit = {
    assertTokens("+ ++", "PLUS(+), WS( ), PLUS(+), PLUS(+)")
  }

  @Test
  def relations(): Unit = {
    assertTokens("==", "EQ_EQ(==)")
    assertTokens("!=", "BANG_EQ(!=)")
    assertTokens("<", "LT(<)")
    assertTokens("<=", "LT_EQ(<=)")
    assertTokens(">", "GT(>)")
    assertTokens(">=", "GT_EQ(>=)")
  }

  @Test
  def logical(): Unit = {
    assertTokens("||", "BAR_BAR(||)")
    assertTokens("&&", "AMP_AMP(&&)")
  }

  @Test
  def identifier(): Unit = {
    assertTokens("a", "IDENT(a)")
    assertTokens("foo", "IDENT(foo)")
    assertTokens("foo1", "IDENT(foo1)")
    assertTokens("foo123", "IDENT(foo123)")
  }

  @Test
  def identifierAsKeyword(): Unit = {
    assertTokens("defdef", "IDENT(defdef)")
  }

  @Test
  def identifiers(): Unit = {
    assertTokens("foo bar", "IDENT(foo), WS( ), IDENT(bar)")
  }

  @Test
  def unknownChar(): Unit = {
    assertTokens("&", "error: UNKNOWN(&)")
  }

  def assertTokens(input: String, expectation: String): Unit = {
    assertEquals(expectation, ToyLexer.analyze(input).map(_.toCompleteString).mkString(", "))
  }
}