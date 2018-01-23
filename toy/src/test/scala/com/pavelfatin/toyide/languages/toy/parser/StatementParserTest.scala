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

package com.pavelfatin.toyide.languages.toy.parser

import org.junit.Test

class StatementParserTest extends ParserTest(StatementParser) {
  @Test
  def function(): Unit = {
      assertParsed("def foo(): integer = {}",
          """
          function
            DEF
            foo
            parameters
              LPAREN
              RPAREN
            typeSpec
              COLON
              INTEGER
            EQ
            block
              LBRACE
              RBRACE
          """)
    }

  @Test
  def blockStatement(): Unit = {
    assertParsed("a = 1;",
      """
      assignment
        referenceToValue
          a
        EQ
        literal
          1
        SEMI
      """)
  }
}