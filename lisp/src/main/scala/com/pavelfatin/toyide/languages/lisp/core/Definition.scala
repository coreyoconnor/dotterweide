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

package com.pavelfatin.toyide.languages.lisp.core

import com.pavelfatin.toyide.Output
import com.pavelfatin.toyide.languages.lisp.value.{Environment, Expression, ListValue, SymbolValue}

object Def extends CoreFunction("def", isLazy = true) {
  def apply(arguments: Seq[Expression], environment: Environment, output: Output): ListValue = arguments match {
    case Seq(SymbolValue(name), expression) =>
      environment.setGlobal(name, expression.eval(environment, output))
      ListValue.Empty
    case _ => expected("symbol expression", arguments, environment)
  }
}

object Let extends CoreFunction("let", isLazy = true) with Bindings {
  def apply(arguments: Seq[Expression], environment: Environment, output: Output): Expression = arguments match {
    case Seq(ListValue(elements), expressions @ _*) =>
      val env = bind(elements, environment, output)
      expressions.map(_.eval(env, output)).lastOption.getOrElse(ListValue.Empty)
    case _ => expected("[bindings*] exprs*", arguments, environment)
  }
}
