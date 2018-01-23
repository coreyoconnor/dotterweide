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

package com.pavelfatin.toyide

import com.pavelfatin.toyide.lexer.{Token, TokenKind, Lexer}

object MockLexer extends Lexer {
  private val TokenPattern = """(\p{Lu}|[(){}])\p{Ll}*""".r

  def analyze(input: CharSequence): Iterator[Token] = {
    TokenPattern.findAllIn(input).matchData.map { m =>
      Token(MockTokenKind, Span(input, m.start, m.end))
    }
  }
}

object MockTokenKind extends TokenKind("token", true)