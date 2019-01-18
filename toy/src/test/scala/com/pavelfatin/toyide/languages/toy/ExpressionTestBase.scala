/*
 *  ExpressionTestBase.scala
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

import org.junit.Test

abstract class ExpressionTestBase {
  @Test
  def literals(): Unit = {
    assertOutput("print(\"foo\");", "foo")

    assertOutput("print(123);", "123")

    assertOutput("print(true);", "true")
    assertOutput("print(false);", "false")
  }

  @Test
  def booleanAnd(): Unit = {
    assertOutput("print(true && true);", "true")
    assertOutput("print(true && false);", "false")
    assertOutput("print(false && true);", "false")
    assertOutput("print(false && false);", "false")
  }

  @Test
  def booleanAndLazyEvaluation(): Unit = {
    assertOutput("def f(): boolean = { print(1); return true; } print(true && f());", "1true")
    assertOutput("def f(): boolean = { print(1); return true; } print(false && f());", "false")
  }

  @Test
  def booleanOr(): Unit = {
    assertOutput("print(true || true);", "true")
    assertOutput("print(true || false);", "true")
    assertOutput("print(false || true);", "true")
    assertOutput("print(false || false);", "false")
  }

  @Test
  def booleanOrLazyEvaluation(): Unit = {
    assertOutput("def f(): boolean = { print(1); return true; } print(true || f());", "true")
    assertOutput("def f(): boolean = { print(1); return true; } print(false || f());", "1true")
  }

  @Test
  def integerGt(): Unit = {
    assertOutput("print(1 > 2);", "false")
    assertOutput("print(2 > 1);", "true")
    assertOutput("print(2 > 2);", "false")
  }

  @Test
  def integerGtEq(): Unit = {
    assertOutput("print(1 >= 2);", "false")
    assertOutput("print(2 >= 1);", "true")
    assertOutput("print(2 >= 2);", "true")
  }

  @Test
  def integerLt(): Unit = {
    assertOutput("print(1 < 2);", "true")
    assertOutput("print(2 < 1);", "false")
    assertOutput("print(2 < 2);", "false")
  }

  @Test
  def integerLtEq(): Unit = {
    assertOutput("print(1 <= 2);", "true")
    assertOutput("print(2 <= 1);", "false")
    assertOutput("print(2 <= 2);", "true")
  }

  @Test
  def stringEq(): Unit = {
    assertOutput("print(\"foo\" == \"foo\");", "true")
    assertOutput("print(\"foo\" == \"bar\");", "false")
  }

  @Test
  def stringEqNonConstant(): Unit = {
    assertOutput("print(\"foo\" + 1 == \"foo2\");", "false")
    assertOutput("print(\"foo\" + 2 == \"foo2\");", "true")
  }

  @Test
  def integerEq(): Unit = {
    assertOutput("print(1 == 1);", "true")
    assertOutput("print(1 == 2);", "false")
  }

  @Test
  def booleanEq(): Unit = {
    assertOutput("print(true == true);", "true")
    assertOutput("print(false == false);", "true")
    assertOutput("print(true == false);", "false")
    assertOutput("print(false == true);", "false")
  }

  @Test
  def stringNotEq(): Unit = {
    assertOutput("print(\"foo\" != \"foo\");", "false")
    assertOutput("print(\"foo\" != \"bar\");", "true")
  }

  @Test
  def stringNotEqNonConstant(): Unit = {
    assertOutput("print(\"foo\" + 2 != \"foo2\");", "false")
    assertOutput("print(\"foo\" + 1 != \"foo2\");", "true")
  }

  @Test
  def integerNotEq(): Unit = {
    assertOutput("print(1 != 1);", "false")
    assertOutput("print(1 != 2);", "true")
  }

  @Test
  def booleanNotEq(): Unit = {
    assertOutput("print(true != true);", "false")
    assertOutput("print(false != false);", "false")
    assertOutput("print(true != false);", "true")
    assertOutput("print(false != true);", "true")
  }

  @Test
  def integerCalculations(): Unit = {
    assertOutput("print(1 + 2);", "3")
    assertOutput("print(3 - 2);", "1")
    assertOutput("print(2 * 3);", "6")
    assertOutput("print(6 / 3);", "2")
    assertOutput("print(6 % 2);", "0")
    assertOutput("print(6 % 4);", "2")
  }

  @Test
  def prefixExpression(): Unit = {
    assertOutput("print(+3);", "3")
    assertOutput("print(-3);", "-3")
    assertOutput("print(--3);", "3")

    assertOutput("print(!true);", "false")
    assertOutput("print(!false);", "true")
    assertOutput("print(!!true);", "true")
  }

  @Test
  def stringConcatenation(): Unit = {
    assertOutput("print(\"foo\" + \"bar\");", "foobar")
    assertOutput("print(\"foo\" + 1);", "foo1")
    assertOutput("print(\"foo\" + true);", "footrue")
  }

  @Test
  def group(): Unit = {
    assertOutput("print((1 + 2));", "3")
  }

  @Test
  def complexExpression(): Unit = {
    assertOutput("print(1 + 2 * 3 * (4 + 5));", "55")
  }

  @Test
  def evaluationOrder(): Unit = {
    assertOutput("""
      def a(): integer = { print(1); return 1; }
      def b(): integer = { print(2); return 2; }
      print(a() + b());
    """, "123")
  }

  protected def assertOutput(code: String, expected: String): Unit
}