/*
 *  TranslationTest.scala
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

package com.pavelfatin.toyide.languages.toy.compiler

import org.junit.{Assert, Test}
import com.pavelfatin.toyide.languages.toy.{EvaluationTestBase, ToyLexer}
import com.pavelfatin.toyide.compiler._
import com.pavelfatin.toyide.MockConsole
import com.pavelfatin.toyide.languages.toy.parser.ProgramParser

class TranslationTest extends EvaluationTestBase with TranslatorTesting {
  @Test(expected = classOf[TranslationException])
  def translationError(): Unit = {
    val root = ProgramParser.parse(ToyLexer.analyze("var a: integer = ;"))
    root.translate("Main", new Labels()).toText("Main")
  }

  def customClassName(): Unit = {
    val root = ProgramParser.parse(ToyLexer.analyze("var a: integer = 1; print(a);"))
    val bytecode = Assembler.assemble(root, "Foo")
    val output = new MockConsole()
    BytecodeInvoker.invoke(bytecode, "Foo", output)
    Assert.assertEquals("1", output.text)
  }

  @Test
  def localsWithLargeIndex(): Unit = {
    assertOutput("def f(a: integer, b: integer, c: integer): void = { var i: integer = 1; }; f(1, 2, 3); ", "")
    assertOutput("def f(a: integer, b: integer, c: integer, d: integer): void = { print(d); }; f(1, 2, 3, 4); ", "4")
    assertOutput("def f(a: integer, b: integer, c: integer, d: integer): void = { d = 1; }; f(1, 2, 3, 4); ", "")
  }

  @Test
  def stackOverflow(): Unit = {
    run("def f(): void = { f(); }")

    try {
      run("def f(): void = { f(); }; f();")
    } catch {
      case InvocationException(message, _) if message == "java.lang.StackOverflowError" => return
    }

    Assert.fail("Expecting stack overflow exception")
  }

  @Test
  def stackOverflowWithParameterAllocations(): Unit = {
    run("def f(p: integer): void = { f(1); }")

    try {
      run("def f(p: integer): void = { f(1); }; f(2);")
    } catch {
      case InvocationException(message, _) if message == "java.lang.StackOverflowError" => return
    }

    Assert.fail("Expecting stack overflow exception")
  }

  @Test
  def simpleTrace(): Unit = {
    try {
      run("""
      print(1);
      print(2);
      print(3 / 0);
      print(4);
      """)
    } catch {
      case InvocationException(_, trace) =>
        Assert.assertEquals(List(Place(None, 3)), trace.toList)
        return
    }

    Assert.fail("Expecting division by zero exception")
  }

  @Test
  def complexTrace(): Unit = {
    try {
      run("""
      def a(): void = {
        print(1 / 0);
      }
      def b(): void = {
        a();
      }
      def c(): void = {
        b();
      }
      c();
      """)
    } catch {
      case InvocationException(_, trace) =>
        val expected = List(
          Place(Some("a"), 2),
          Place(Some("b"), 5),
          Place(Some("c"), 8),
          Place(None, 10))

        Assert.assertEquals(expected, trace.toList)

        return
    }

    Assert.fail("Expecting division by zero exception")
  }
}