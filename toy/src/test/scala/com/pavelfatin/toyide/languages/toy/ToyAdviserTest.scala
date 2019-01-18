/*
 *  ToyAdviserTest.scala
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

package com.pavelfatin.toyide.languages.toy

import com.pavelfatin.toyide.editor.AdviserTestBase
import com.pavelfatin.toyide.languages.toy.parser.ProgramParser
import org.junit.Test

class ToyAdviserTest extends AdviserTestBase(ToyLexer, ProgramParser, ToyAdviser) {
  @Test
  def insideEmptyProgram(): Unit = {
    assertVariantsAre("|")("print", "println", "def", "var", "if", "while")
  }

  @Test
  def atIdentifier(): Unit = {
    assertVariantsAre("def |")()
    assertVariantsAre("def foo|")()
    assertVariantsAre("def |foo")()
    assertVariantsAre("def foo|bar")()

    assertVariantsAre("var |")()
    assertVariantsAre("var foo|")()
    assertVariantsAre("var |foo")()
    assertVariantsAre("var foo|bar")()
  }

  @Test
  def afterKeyword(): Unit = {
    assertVariantsAre("if |")()
    assertVariantsAre("while |")()
  }

  @Test
  def afterIf(): Unit = {
    assertVariantsAre("if (true) {} |")("print", "println", "def", "var", "else", "if", "while")
    assertVariantsAre("if (true) {} else {} |")("print", "println", "def", "var", "if", "while")
  }

  @Test
  def insideControlStructure(): Unit = {
    assertVariantsAre("if (true) {|}")("print", "println", "if", "while")
  }

  @Test
  def afterIfInsideControlStructure(): Unit = {
    assertVariantsAre("if (true) { if (true) {} |}")("print", "println", "else", "if", "while")
  }

  @Test
  def insideFunction(): Unit = {
    assertVariantsAre("def f(): void = {|}")("f", "print", "println", "return", "if", "while")
    assertVariantsAre("def f(): void = { if (true) {|} }")("f", "print", "println", "return", "if", "while")
  }

  @Test
  def insideTypeSpecifier(): Unit = {
    assertVariantsAre("var v:|")("string", "integer", "boolean")
    assertVariantsAre("var v:| = true;")("string", "integer", "boolean")

    assertVariantsAre("def f(p:|")("string", "integer", "boolean")
    assertVariantsAre("def f(p:|): void = {}")("string", "integer", "boolean")

    assertVariantsAre("def f():|")("void", "string", "integer", "boolean")
    assertVariantsAre("def f():| = {}")("void", "string", "integer", "boolean")
  }

  @Test
  def insideExpression(): Unit = {
    assertVariantsAre("if (|")("true", "false")
    assertVariantsAre("if (|) {}")("true", "false")
  }

  @Test
  def insideArguments(): Unit = {
    assertVariantsAre("f(|")("true", "false")
    assertVariantsAre("f(|);")("true", "false")
  }

  @Test
  def variable(): Unit = {
    assertVariantsAre("var a: integer = 1;|")("a", "print", "println", "def", "var", "if", "while")
    assertVariantsAre("|;var a: integer = 1")("print", "println", "def", "var", "if", "while")
  }

  @Test
  def function(): Unit = {
    assertVariantsAre("def f(): void = {};|")("f", "print", "println", "def", "var", "if", "while")
    assertVariantsAre("def f(): void = {|}")("f", "print", "println", "return", "if", "while")
    assertVariantsAre("|;def f(): void = {}")("print", "println", "def", "var", "if", "while")
  }

  @Test
  def parameter(): Unit = {
    assertVariantsAre("def f(p: integer): void = {|}")("p", "f", "print", "println", "return", "if", "while")
    assertVariantsAre("def f(p: integer): void = {};|")("f", "print", "println", "def", "var", "if", "while")
  }

  @Test
  def insideBinaryExpression(): Unit = {
    assertVariantsAre("if (true && |")("true", "false")
    assertVariantsAre("if (true && |) {}")("true", "false")
  }

  @Test
  def insidePrefixExpression(): Unit = {
    assertVariantsAre("if (!|")("true", "false")
    assertVariantsAre("if (!|) {}")("true", "false")
  }

  @Test
  def referenceInsideHolders(): Unit = {
    assertVariantsAre("var a: integer = 1; if (|")("a", "true", "false")
    assertVariantsAre("var a: integer = 1; foo(|")("a", "true", "false")
    assertVariantsAre("var a: integer = 1; var b:|")("string", "integer", "boolean")
    assertVariantsAre("var a: integer = 1; if (2 + |")("a", "true", "false")
  }

  @Test
  def sorting(): Unit = {
    assertVariantsAre("def f(a: integer, b: integer): void = { if (|) }")("a", "b", "f", "true", "false")
    assertVariantsAre("def f(b: integer, a: integer): void = { if (|) }")("a", "b", "f", "true", "false")

    assertVariantsAre("def a(): void = {}; def b(): void = {}; if (|)")("a", "b", "true", "false")
    assertVariantsAre("def b(): void = {}; def a(): void = {}; if (|)")("a", "b", "true", "false")

    assertVariantsAre("var a: integer = 1; var b: integer = 2; if (|)")("a", "b", "true", "false")
    assertVariantsAre("var b: integer = 2; var a: integer = 1; if (|)")("a", "b", "true", "false")
  }

  @Test
  def order(): Unit = {
    assertVariantsAre("var a: integer = 1; def b(): void = {}; def f(c: integer): void = { if (|) }")("c", "b", "f", "a", "true", "false")
  }

  @Test
  def duplicates(): Unit = {
    assertVariantsAre("var v: integer = 1; var v: integer = 1; if (|)")("v", "true", "false")
    assertVariantsAre("var v: integer = 1; def f(v: integer): void = { if (|) }")("v", "f", "true", "false")
  }

  @Test
  def variableItself(): Unit = {
    assertVariantsAre("var a: integer = |")("true", "false")
    assertVariantsAre("var a: integer = 1; var b: integer = |")("a", "true", "false")
  }
}