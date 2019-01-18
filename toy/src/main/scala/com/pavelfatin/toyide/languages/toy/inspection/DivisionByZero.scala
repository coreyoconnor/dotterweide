/*
 *  DivisionByZero.scala
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

package com.pavelfatin.toyide.languages.toy.inspection

import com.pavelfatin.toyide.inspection.{Decoration, Inspection, Mark}
import com.pavelfatin.toyide.languages.toy.ToyTokens._
import com.pavelfatin.toyide.languages.toy.ToyType._
import com.pavelfatin.toyide.languages.toy.node.BinaryExpression
import com.pavelfatin.toyide.lexer.Token
import com.pavelfatin.toyide.node.{Expression, Node, NodeToken}

object DivisionByZero extends Inspection {
  val Message = "Division by zero"

  def inspect(node: Node): Seq[Mark] = node match {
    case e: BinaryExpression => e.children match {
        case Expression(IntegerType) :: NodeToken(Token(SLASH, _, _)) ::  (r @ Expression(IntegerType)) :: Nil
          if r.optimized.contains("0") => Mark(e, Message, Decoration.Fill, warning = true) :: Nil
        case _ => Nil
      }
    case _ => Nil
  }
}