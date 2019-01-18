/*
 *  ReferenceTest.scala
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
import dotterweide.node.{Node, ReferenceNode}
import dotterweide.Extensions._
import dotterweide.Helpers._
import dotterweide.languages.toy.parser.ProgramParser

class ReferenceTest {
  @Test
  def noDeclarations(): Unit = {
    assertMatches(targetIn("v = 1;")) {
      case None =>
    }
    assertMatches(targetIn("def foo(): integer = { p = 1; }")) {
      case None =>
    }
    assertMatches(targetIn("f();")) {
      case None =>
    }
  }

  @Test
  def nameDiffers(): Unit = {
    assertMatches(targetIn("var x: integer = 1; v = 1;")) {
      case None =>
    }
    assertMatches(targetIn("def foo(x: integer): integer = { p = 1; }")) {
      case None =>
    }
    assertMatches(targetIn("def x(): integer = {}; f();")) {
      case None =>
    }
  }

  @Test
  def declared(): Unit = {
    assertMatches(targetIn("var v: integer = 1; v = 1;")) {
      case Some(Offset(0)) =>
    }
    assertMatches(targetIn("def foo(p: integer): integer = { p = 1; }")) {
      case Some(Offset(8)) =>
    }
    assertMatches(targetIn("def f(): integer = {}; f();")) {
      case Some(Offset(0)) =>
    }
  }

  @Test
  def severalDeclarations(): Unit = {
    assertMatches(targetIn("var v: integer = 1; var x: integer = 1; v = 1;")) {
      case Some(Offset(0)) =>
    }
    assertMatches(targetIn("def foo(p: integer, x: integer): integer = { p = 1; }")) {
      case Some(Offset(8)) =>
    }
    assertMatches(targetIn("def f(): integer = {}; def x(): integer = {}; f();")) {
      case Some(Offset(0)) =>
    }
  }

  @Test
  def severalDeclarationsReversed(): Unit = {
    assertMatches(targetIn("var x: integer = 1; var v: integer = 1; v = 1;")) {
      case Some(Offset(20)) =>
    }
    assertMatches(targetIn("def foo(x: integer, p: integer): integer = { p = 1; }")) {
      case Some(Offset(20)) =>
    }
    assertMatches(targetIn("def x(): integer = {}; def f(): integer = {}; f();")) {
      case Some(Offset(23)) =>
    }
  }

  @Test
  def incorrectOrder(): Unit = {
    assertMatches(targetIn("v = 1; var v: integer = 1;")) {
      case None =>
    }
    assertMatches(targetIn("p = 1; def foo(p: integer): integer = {}")) {
      case None =>
    }
    assertMatches(targetIn("f(); def f(): integer = {}")) {
      case None =>
    }
  }

  @Test
  def incorrectKind(): Unit = {
    assertMatches(targetIn("var v: integer = 1; v();")) {
      case None =>
    }
    assertMatches(targetIn("def foo(p: integer): integer = { p(); }")) {
      case None =>
    }
    assertMatches(targetIn("def f(): integer = {}; f = 1;")) {
      case None =>
    }
  }

  @Test
  def outerScope(): Unit = {
    assertMatches(targetIn("var v: integer = 1; while (true) { v = 1; }")) {
      case Some(Offset(0)) =>
    }
    assertMatches(targetIn("def foo(p: integer): integer = { while (true) { p = 1; } }")) {
      case Some(Offset(8)) =>
    }
    assertMatches(targetIn("def f(): integer = {}; while (true) { f(); }")) {
      case Some(Offset(0)) =>
    }
  }

  @Test
  def innerScope(): Unit = {
    assertMatches(targetIn("while (true) { var a: integer = 1; }; a = 1;")) {
      case None =>
    }
    assertMatches(targetIn("def foo(p: integer): integer = {};  p = 1;")) {
      case None =>
    }
  }

  @Test
  def scopePreference(): Unit = {
    assertMatches(targetIn("var v: integer = 1; while (true) { var v: integer = 1; v = 1; }")) {
      case Some(Offset(35)) =>
    }
    assertMatches(targetIn("var p: integer = 1; def f(p: integer): integer = { p = 1; };")) {
      case Some(Offset(26)) =>
    }
  }

  @Test
  def selfScope(): Unit = {
    assertMatches(targetIn("def f(): integer = { f(); }")) {
      case Some(Offset(0)) =>
    }
  }

  def targetIn(code: String): Option[Node] = {
    val elements = ProgramParser.parse(ToyLexer.analyze(code)).elements
    assertNoProblemsIn(elements)
    elements.findBy[ReferenceNode].flatMap(_.target)
  }
}