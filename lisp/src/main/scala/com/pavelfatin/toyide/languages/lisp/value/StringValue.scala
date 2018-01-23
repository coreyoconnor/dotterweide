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

package com.pavelfatin.toyide.languages.lisp.value

import com.pavelfatin.toyide.Extensions._

object StringValue {
  def apply(s: String): ListValue =
    ListValue(s.map(CharacterValue))

  def unapply(list: ListValue): Option[String] = {
    val elements = list.content

    if (elements.isEmpty) None else elements.collectAll {
      case CharacterValue(c) => c
    } map { chars =>
      chars.mkString
    }
  }
}
