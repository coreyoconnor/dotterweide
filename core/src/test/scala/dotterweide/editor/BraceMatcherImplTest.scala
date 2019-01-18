/*
 *  BraceMatcherImplTest.scala
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

package dotterweide.editor

import org.junit.Test
import org.junit.Assert._
import dotterweide.MockLexer
import dotterweide.lexer.{Token, TokenKind}

class BraceMatcherImplTest {
  private val LPAREN = TokenKind("LPAREN")

  private val RPAREN = TokenKind("RPAREN")

  private val matcher = new BraceMatcherImpl(Seq((LPAREN, RPAREN)))

  @Test
  def unavailable(): Unit = {
    assertEmphasized("|")
    assertEmphasized("|Foo")
    assertEmphasized("Foo|")
    assertEmphasized("Foo|Bar")
  }

  @Test
  def empty(): Unit = {
    assertEmphasized("|X()")
    assertEmphasized("|()", 0, 1)
    assertEmphasized("(|)")
    assertEmphasized("()|", 0, 1)
    assertEmphasized("()X|")
  }

  @Test
  def oneToken(): Unit = {
    assertEmphasized("|X(Foo)")

    assertEmphasized("|(Foo)", 0, 4)

    assertEmphasized("(|Foo)")
    assertEmphasized("(F|oo)")
    assertEmphasized("(Fo|o)")
    assertEmphasized("(Foo|)")

    assertEmphasized("(Foo)|", 0, 4)

    assertEmphasized("(Foo)X|")
  }

  def joined(): Unit = {
    assertEmphasized("()|()", 0, 1, 2, 3)
    assertEmphasized("(Foo)|(Bar)", 0, 4, 5, 9)
    assertEmphasized("(FooBar)|(FooMoo)", 0, 7, 8, 15)
  }

  @Test
  def unbalanced(): Unit = {
    assertEmphasized("|(Foo", -1)
    assertEmphasized("(|Foo")
    assertEmphasized("|(FooBar", -1)
    assertEmphasized("Foo|)")
    assertEmphasized("Foo)|", -4)
    assertEmphasized("FooBar)|", -7)
  }

  @Test
  def nested(): Unit = {
    assertEmphasized("|((Foo)Bar)", 0, 9)
    assertEmphasized("(|(Foo)Bar)", 1, 5)
    assertEmphasized("((|Foo)Bar)")
    assertEmphasized("((Foo)|Bar)", 1, 5)
    assertEmphasized("((Foo)Bar|)")
    assertEmphasized("((Foo)Bar)|", 0, 9)
  }

  @Test
  def nestedUnbalanced(): Unit = {
    assertEmphasized("|((FooBar)", -1)
    assertEmphasized("(|(FooBar)", 1, 8)
    assertEmphasized("((|FooBar)")
    assertEmphasized("((FooBar)|", 1, 8)

    assertEmphasized("|(Foo)Bar)", 0, 4)
    assertEmphasized("(|Foo)Bar)")
    assertEmphasized("(Foo)Bar|)")
    assertEmphasized("(Foo)Bar)|", -9)
  }

  private def assertEmphasized(code: String, indices: Int*): Unit = {
    val offset = code.indexOf("|")

    val mapped = MockLexer.analyze(code.replace("|", "")).map { it =>
      it.span.text match {
        case "(" => it.copy(kind = LPAREN)
        case ")" => it.copy(kind = RPAREN)
        case _ => it
      }
    }.toList

    def indexOf(token: Token): Option[Int] = {
      matcher.braceTypeOf(token, mapped, offset) match {
        case Inapplicable => None
        case Paired => Some(token.span.begin)
        case Unbalanced => Some(-token.span.begin - 1)
      }
    }

    val actual = mapped.flatMap(indexOf(_).toSeq)

    assertEquals(indices.toList, actual)
  }
}