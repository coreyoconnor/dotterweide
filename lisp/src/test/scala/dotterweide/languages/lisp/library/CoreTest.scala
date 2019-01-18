/*
 *  CoreTest.scala
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

package dotterweide.languages.lisp.library

import org.junit.Test

class CoreTest extends LibraryTestBase {
  @Test
  def defmacro(): Unit = {
    assertValue("(macroexpand '(defmacro m [x] 1))", "(def m (macro m (x) 1))")
  }

  @Test
  def defn(): Unit = {
    assertValue("(macroexpand '(defn f [x] 1))", "(def f (fn f (x) 1))")
  }

  @Test
  def defnPloy(): Unit = {
    assertValue("(defn-poly f ([] 1)) (f)", "1")
    assertValue("(defn-poly f ([x] x)) (f 1)", "1")
    assertValue("(defn-poly f ([x y] x)) (f 1 2)", "1")
    assertValue("(defn-poly f ([x y] y)) (f 1 2)", "2")

    assertValue("(defn-poly f ([x] x) ([x y] (+ x y))) (f 1)", "1")
    assertValue("(defn-poly f ([x] x) ([x y] (+ x y))) (f 1 2)", "3")

    assertError("(defn-poly f ([x y] x)) (f 1)", "argument count")
    assertError("(defn-poly f ([x y] x)) (f 1 2 3)", "argument count")
  }

  @Test
  def isTrue(): Unit = {
    assertValue("(true? true)", "true")
    assertValue("(true? false)", "false")

    assertValue("(true? 1)", "false")
  }

  @Test
  def isFalse(): Unit = {
    assertValue("(false? true)", "false")
    assertValue("(false? false)", "true")

    assertValue("(false? 1)", "false")
  }

  @Test
  def when(): Unit = {
    assertValue("(when true)", "()")

    assertValue("(when true 1)", "1")
    assertValue("(when true 1 2)", "2")

    assertValue("(when false 1)", "()")

    assertOutput("(when true (trace 1) (trace 2))", "12")
    assertOutput("(when false (trace 1) (trace 2))", "")
  }

  @Test
  def cond(): Unit = {
    assertValue("(cond)", "()")

    assertValue("(cond true 1)", "1")
    assertValue("(cond false 1)", "()")

    assertValue("(cond true 1 true 2)", "1")
    assertValue("(cond false 1 true 2)", "2")
    assertValue("(cond false 1 false 2)", "()")

    assertOutput("(cond (trace true) (trace 1) (trace true) (trace 2))", "true1")
  }

  @Test
  def elseForm(): Unit = {
    assertValue("else", "true")
  }

  @Test
  def ifLet(): Unit = {
    assertValue("(if-let [x true] 1)", "1")
    assertValue("(if-let [x false] 1)", "()")

    assertValue("(if-let [x true] 1 2)", "1")
    assertValue("(if-let [x false] 1 2)", "2")

    assertValue("(if-let [x '(1 2 3)] 1 2)", "1")
    assertValue("(if-let [x nil] 1 2)", "2")

    assertOutput("(if-let [x (trace true)] (trace 1) (trace 2))", "true1")
    assertOutput("(if-let [x (trace false)] (trace 1) (trace 2))", "false2")
  }

  @Test
  def threadSecond(): Unit = {
    assertValue("(-> 4)", "4")
    assertValue("(-> 4 inc)", "5")

    assertValue("(-> 4 inc list)", "(5)")

    assertValue("(-> 6 (/ 2))", "3")
    assertValue("(-> 12 (/ 2) (/ 3))", "2")

    assertValue("(-> 12 (/ 2 3))", "2")
  }

  @Test
  def threadLast(): Unit = {
    assertValue("(->> 4)", "4")
    assertValue("(->> 4 inc)", "5")

    assertValue("(->> 4 inc list)", "(5)")

    assertValue("(->> 2 (/ 6))", "3")
    assertValue("(->> 2 (/ 6) (/ 12))", "4")

    assertValue("(->> 3 (/ 12 2))", "2")
  }
}
