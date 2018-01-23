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

package com.pavelfatin.toyide.languages.lisp.node

import com.pavelfatin.toyide.Extensions._
import com.pavelfatin.toyide.languages.lisp.LispTokens._
import com.pavelfatin.toyide.languages.lisp.value.ListValue
import com.pavelfatin.toyide.node.NodeImpl

class ListNode extends NodeImpl("list") with ExpressionNode {
  protected def read0(source: String): ListValue = prefixKind match {
    case Some(HASH) => FunctionLiteral.readFrom(this, source)
    case _ => ListValue(expressions.map(_.read(source)), Some(placeIn(source)))
  }

  def expressions: Seq[ExpressionNode] = children.filterBy[ExpressionNode]

  def function: Option[ExpressionNode] = expressions.headOption

  def arguments: Seq[ExpressionNode] = expressions.drop(1)
}

object ListNode {
  def unapplySeq(node: ListNode) = Some(node.expressions)
}
