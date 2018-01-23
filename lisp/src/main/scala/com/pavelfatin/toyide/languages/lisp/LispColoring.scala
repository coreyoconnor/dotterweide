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

import java.awt.Color

import com.pavelfatin.toyide.editor.{AbstractColoring, Attributes, Coloring, Style, Weight}
import com.pavelfatin.toyide.languages.lisp.LispTokens._
import com.pavelfatin.toyide.lexer.TokenKind

class LispColoring(colors: Map[String, Color]) extends AbstractColoring(colors) {
  def attributesFor(kind: TokenKind): Attributes = {
    val foreground  = apply(colorId(kind))
    val weight      = weightFor(kind)
    val style       = styleFor(kind)
    Attributes(foreground, None, weight, style, underlined = false)
  }

  private def colorId(kind: TokenKind) = kind match {
    case COMMENT            => Coloring.Comment
    case BOOLEAN_LITERAL    => Coloring.BooleanLiteral
    case INTEGER_LITERAL    => Coloring.IntegerLiteral
    case CHARACTER_LITERAL  => Coloring.CharLiteral
    case STRING_LITERAL     => Coloring.StringLiteral
    case PREDEFINED_SYMBOL  => Coloring.Keyword
    case _                  => Coloring.TextForeground
  }

  private def weightFor(token: TokenKind) = token match {
    case BOOLEAN_LITERAL    => Weight.Bold
    case STRING_LITERAL     => Weight.Bold
    case PREDEFINED_SYMBOL  => Weight.Bold
    case _                  => Weight.Normal
  }

  private def styleFor(token: TokenKind) = token match {
    case COMMENT  => Style.Italic
    case _        => Style.Ordinary
  }
}
