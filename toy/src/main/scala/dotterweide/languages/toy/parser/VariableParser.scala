/*
 *  VariableParser.scala
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

package dotterweide.languages.toy.parser

import dotterweide.languages.toy.ToyTokens._
import dotterweide.languages.toy.node.VariableDeclaration
import dotterweide.parser.{SyncParser, TreeBuilder}

object VariableParser extends SyncParser {
  def parseTo(in: TreeBuilder): Unit =
    in.capturing(new VariableDeclaration()) {
      in.consume(VAR)
      in.consume(IDENT)
      TypeSpecParser.parseTo(in)
      in.consume(EQ)
      ExpressionParser.parseTo(in)
      in.consume(SEMI)
    }
}