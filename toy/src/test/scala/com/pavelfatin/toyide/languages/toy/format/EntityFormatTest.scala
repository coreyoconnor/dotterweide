/*
 *  EntityFormatTest.scala
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

package com.pavelfatin.toyide.languages.toy.format

import com.pavelfatin.toyide.languages.toy.parser._
import org.junit.Test

class EntityFormatTest extends FormatTestBase {
  @Test
  def typeSpec(): Unit = {
    assertFormatted(":void", TypeSpecParser, ": void")
    assertFormatted(": void", TypeSpecParser, ": void")
    assertFormatted(":  void", TypeSpecParser, ": void")
  }

  @Test
  def typedIdent(): Unit = {
    assertFormatted("foo:integer", ParameterParser, "foo: integer")
    assertFormatted("foo: integer", ParameterParser, "foo: integer")
    assertFormatted("foo:  integer", ParameterParser, "foo: integer")
    assertFormatted("foo :integer", ParameterParser, "foo: integer")
  }

  @Test
  def parameters(): Unit = {
    assertFormatted("()", ParametersParser, "()")
    assertFormatted("(a: integer)", ParametersParser, "(a: integer)")
    assertFormatted("( a: integer)", ParametersParser, "(a: integer)")
    assertFormatted("(a: integer )", ParametersParser, "(a: integer)")
    assertFormatted("( a: integer )", ParametersParser, "(a: integer)")
    assertFormatted("(a: integer,b: integer)", ParametersParser, "(a: integer, b: integer)")
    assertFormatted("(a: integer, b: integer)", ParametersParser, "(a: integer, b: integer)")
    assertFormatted("(a: integer,  b: integer)", ParametersParser, "(a: integer, b: integer)")
    assertFormatted("(  a: integer,  b: integer  )", ParametersParser, "(a: integer, b: integer)")
  }

  @Test
  def block(): Unit = {
    assertFormatted("{}", BlockParser, "{\n}")
  }

  @Test
  def function(): Unit = {
    assertFormatted("def  foo(): void={\n}", FunctionParser, "def foo(): void = {\n}")
    assertFormatted("def foo(): void={\n}", FunctionParser, "def foo(): void = {\n}")
    assertFormatted("def foo(): void ={\n}", FunctionParser, "def foo(): void = {\n}")
    assertFormatted("def foo(): void  ={\n}", FunctionParser, "def foo(): void = {\n}")
    assertFormatted("def foo(): void= {\n}", FunctionParser, "def foo(): void = {\n}")
    assertFormatted("def foo(): void=  {\n}", FunctionParser, "def foo(): void = {\n}")
    assertFormatted("def foo(): void = {\n}", FunctionParser, "def foo(): void = {\n}")
    assertFormatted("def foo(): void  =  {\n}", FunctionParser, "def foo(): void = {\n}")
  }

  @Test
  def variable(): Unit = {
    assertFormatted("var  foo: integer=1;", VariableParser, "var foo: integer = 1;")
    assertFormatted("var foo: integer=1 ;", VariableParser, "var foo: integer = 1;")
    assertFormatted("var foo: integer=1;", VariableParser, "var foo: integer = 1;")
    assertFormatted("var foo: integer =1;", VariableParser, "var foo: integer = 1;")
    assertFormatted("var foo: integer  =1;", VariableParser, "var foo: integer = 1;")
    assertFormatted("var foo: integer= 1;", VariableParser, "var foo: integer = 1;")
    assertFormatted("var foo: integer=  1;", VariableParser, "var foo: integer = 1;")
    assertFormatted("var foo: integer = 1;", VariableParser, "var foo: integer = 1;")
    assertFormatted("var foo: integer  =  1;", VariableParser, "var foo: integer = 1;")
  }

  @Test
  def returnStatement(): Unit = {
    assertFormatted("return 1;", ReturnParser, "return 1;")
    assertFormatted("return  1;", ReturnParser, "return 1;")
    assertFormatted("return 1 ;", ReturnParser, "return 1;")
  }

  @Test
  def arguments(): Unit = {
    assertFormatted("()", ArgumentsParser, "()")
    assertFormatted("(1)", ArgumentsParser, "(1)")
    assertFormatted("( 1)", ArgumentsParser, "(1)")
    assertFormatted("(1 )", ArgumentsParser, "(1)")
    assertFormatted("( 1 )", ArgumentsParser, "(1)")
    assertFormatted("(1,2)", ArgumentsParser, "(1, 2)")
    assertFormatted("(1, 2)", ArgumentsParser, "(1, 2)")
    assertFormatted("(1,  2)", ArgumentsParser, "(1, 2)")
    assertFormatted("(  1,  2  )", ArgumentsParser, "(1, 2)")
  }

  @Test
  def callExpression(): Unit = {
    assertFormatted("foo()", CallExpressionParser, "foo()")
    assertFormatted("foo ()", CallExpressionParser, "foo()")
  }

  @Test
  def call(): Unit = {
    assertFormatted("foo();", CallParser, "foo();")
    assertFormatted("foo() ;", CallParser, "foo();")
  }

  @Test
  def ifStatement(): Unit = {
    assertFormatted("if(true) {\n}", IfParser, "if (true) {\n}")
    assertFormatted("if (true) {\n}", IfParser, "if (true) {\n}")
    assertFormatted("if  (true) {\n}", IfParser, "if (true) {\n}")
    assertFormatted("if (true){\n}", IfParser, "if (true) {\n}")
    assertFormatted("if (true)  {\n}", IfParser, "if (true) {\n}")
    assertFormatted("if (true) {\n}else{\n}", IfParser, "if (true) {\n} else {\n}")
    assertFormatted("if (true) {\n}  else {\n}", IfParser, "if (true) {\n} else {\n}")
    assertFormatted("if (true) {\n} else  {\n}", IfParser, "if (true) {\n} else {\n}")
    assertFormatted("if (true) {\n} else {\n}", IfParser, "if (true) {\n} else {\n}")
  }

  @Test
  def whileStatement(): Unit = {
    assertFormatted("while(true) {\n}", WhileParser, "while (true) {\n}")
    assertFormatted("while (true) {\n}", WhileParser, "while (true) {\n}")
    assertFormatted("while  (true) {\n}", WhileParser, "while (true) {\n}")
    assertFormatted("while (true){\n}", WhileParser, "while (true) {\n}")
    assertFormatted("while (true)  {\n}", WhileParser, "while (true) {\n}")
  }

  @Test
  def assignment(): Unit = {
    assertFormatted("foo=1;", AssignmentParser, "foo = 1;")
    assertFormatted("foo=1 ;", AssignmentParser, "foo = 1;")
    assertFormatted("foo =1;", AssignmentParser, "foo = 1;")
    assertFormatted("foo  =1;", AssignmentParser, "foo = 1;")
    assertFormatted("foo =1;", AssignmentParser, "foo = 1;")
    assertFormatted("foo = 1;", AssignmentParser, "foo = 1;")
    assertFormatted("foo =  1;", AssignmentParser, "foo = 1;")
  }

  @Test
  def comment(): Unit = {
    assertFormatted("// some stuff", CommentParser, "// some stuff")
  }
}