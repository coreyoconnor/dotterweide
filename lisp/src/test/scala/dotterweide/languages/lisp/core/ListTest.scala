/*
 *  ListTest.scala
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

package dotterweide.languages.lisp.core

import dotterweide.languages.lisp.InterpreterTesting
import org.junit.Test

class ListTest extends InterpreterTesting {
  @Test
  def list(): Unit = {
    assertValue("(list)", "()")
    assertValue("(list 1)", "(1)")
    assertValue("(list 1 2 3)", "(1 2 3)")
    assertValue("(list 1 true \\c (list))", "(1 true \\c ())")

    assertOutput("(list (trace 1) (trace 2) (trace 3))", "123")

    assertValue("list", "core.list")
    assertValue("(def f list) (f 1 2 3)", "(1 2 3)")
  }

  @Test
  def cons(): Unit = {
    assertValue("(cons 1 (list))", "(1)")
    assertValue("(cons 1 (cons 2 (list)))", "(1 2)")
    assertValue("(cons 1 (cons 2 (list 3)))", "(1 2 3)")

    assertOutput("(list (trace 1) (trace (list)))", "1()")

    assertValue("cons", "core.cons")
    assertValue("(def f cons) (f 1 (list))", "(1)")

    assertError("(cons)")
    assertError("(cons 1)")
    assertError("(cons (list))")
    assertError("(cons 1 2)")
    assertError("(cons (list) 2)")
    assertError("(cons 1 (list) 1)")
    assertError("(cons 1 (list) (list))")
  }

  @Test
  def first(): Unit = {
    assertValue("(first (list 1))", "1")
    assertValue("(first (list 1 2))", "1")
    assertValue("(first (list 1 2 3))", "1")

    assertValue("first", "core.first")
    assertValue("(def f first) (f (list 1 2 3))", "1")

    assertError("(first (list))", "empty")

    assertError("(first)")
    assertError("(first 1)")
    assertError("(first (list 1) (list 1))")
  }

  @Test
  def rest(): Unit = {
    assertValue("(rest (list 1))", "()")
    assertValue("(rest (list 1 2))", "(2)")
    assertValue("(rest (list 1 2 3))", "(2 3)")

    assertValue("rest", "core.rest")
    assertValue("(def f rest) (f (list 1 2 3))", "(2 3)")

    assertError("(rest (list))", "empty")

    assertError("(rest)")
    assertError("(rest 1)")
    assertError("(rest (list 1) (list 1))")
  }
}