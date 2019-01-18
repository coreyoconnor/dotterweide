/*
 *  InterpreterTesting.scala
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

package com.pavelfatin.toyide.languages.lisp

import com.pavelfatin.toyide.Helpers._
import com.pavelfatin.toyide.MockConsole
import com.pavelfatin.toyide.interpreter.{EvaluationException, Value}
import com.pavelfatin.toyide.languages.lisp.node.ProgramNode
import com.pavelfatin.toyide.languages.lisp.value.{Environment, EnvironmentImpl}
import org.junit.Assert._

trait InterpreterTesting {
  protected def createEnvironment(): Environment = new EnvironmentImpl()

  protected def assertOutput(code: String, expected: String): Unit =
    assertEquals(expected, run(code)._2)

  protected def assertValue(code: String, expected: String): Unit =
    assertEquals(expected, run(code)._1.presentation)

  protected def assertOK(code: String): Unit = run(code)

  protected def assertError(code: String, expected: String = ""): Unit =
    try {
      run(code)
      fail("Error expected: " + expected)
    } catch {
      case EvaluationException(message, _) =>
        assertTrue("Expected: " + expected + ", actual: " + message, message.contains(expected))
    }

  protected def run(code: String, environment: Environment = createEnvironment()): (Value, String) =
    InterpreterTesting.run(code.stripMargin, environment)
}

object InterpreterTesting {
  val Source = "Test"

  def parse(code: String): ProgramNode = {
    val root = LispParser.parse(LispLexer.analyze(code))
    val elements = root.elements
    assertNoProblemsIn(elements)
    root.asInstanceOf[ProgramNode]
  }

  def run(code: String, environment: Environment): (Value, String) = {
    val root = parse(code)
    val console = new MockConsole()
    val value = root.evaluate(Source, environment, console)
    (value, console.text)
  }
}
