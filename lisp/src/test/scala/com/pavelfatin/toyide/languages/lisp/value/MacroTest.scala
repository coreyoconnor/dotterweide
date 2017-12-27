/*
 * Copyright (C) 2014 Pavel Fatin <http://pavelfatin.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.pavelfatin.toyide.languages.lisp.value

import com.pavelfatin.toyide.languages.lisp.InterpreterTesting
import org.junit.Test

class MacroTest extends InterpreterTesting {
  @Test
  def presentation(): Unit = {
    assertValue("(macro [] 1)", "macro0")
    assertValue("(macro [x] 1)", "macro1")
    assertValue("(macro [x y] 1)", "macro2")

    assertValue("(macro [&])", "macro0")
    assertValue("(macro [& l])", "macro0*")
    assertValue("(macro [x & l])", "macro1*")
    assertValue("(macro [x y & l])", "macro2*")

    assertValue("(macro named [])", "named_macro0")
  }

  @Test
  def evaluation(): Unit = {
    assertError("(eval (macro []))")
  }

  @Test
  def application(): Unit = {
    assertValue("((macro [] 1))", "1")
    assertValue("((macro [] 1 2))", "2")

    assertOutput("((macro [] (trace 1) (trace 2)))", "12")
    assertOutput("((macro [x] 1) (trace 2))", "")
    assertOutput("((macro [x] x 2) (trace 2))", "")
  }
}