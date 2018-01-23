/*
 * Copyright 2018 Pavel Fatin, https://pavelfatin.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.pavelfatin.toyide.languages.lisp

import com.pavelfatin.toyide.editor.{Adviser, AdviserTestBase}
import org.junit.Test

class LispAdviserTest extends AdviserTestBase(LispLexer, LispParser, LispAdviser) {
  @Test
  def coreSymbol(): Unit = {
    assertVariantsInclude("|")("def")
  }

  @Test
  def librarySymbol(): Unit = {
    assertVariantsInclude("|")("map")
  }

  @Test
  def userSymbol(): Unit = {
    assertVariantsInclude("(def someSymbol 1) |")("someSymbol")
  }

  @Test
  def anhorExclusion(): Unit = {
    assertVariantsExclude("(def |)")(Adviser.Anchor)
  }
}
