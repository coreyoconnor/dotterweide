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

package com.pavelfatin.toyide.ide

import java.awt.Color

import com.pavelfatin.toyide.Observable
import com.pavelfatin.toyide.editor.{Attributes, Coloring}
import com.pavelfatin.toyide.lexer.TokenKind

private class DynamicColoring(delegates: Map[String, Coloring]) extends Coloring with Observable {
  private var _name     : String    = delegates.head._1
  private var _coloring : Coloring  = delegates.head._2

  def names: Seq[String] = delegates.keys.toSeq
  
  def name: String = _name 
  
  def name_=(name: String): Unit =
    if (_name != name) {
      _name = name
      _coloring = delegates(name)

      notifyObservers()
    }

  def apply(id: String): Color = _coloring(id)

  def fontFamily: String = _coloring.fontFamily

  def fontSize: Int = _coloring.fontSize

  def attributesFor(kind: TokenKind): Attributes = _coloring.attributesFor(kind)
}
