/*
 *  package.scala
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

package dotterweide.editor

import dotterweide.Interval
import dotterweide.document.Document
import dotterweide.node.{IdentifiedNode, Node, ReferenceNode, ReferenceNodeTarget}

package object controller {
  private[controller] implicit class DataExt(val data: Data) extends AnyVal {
    def leafAt(offset: Int): Option[Node] = data.structure.flatMap { root =>
      root.offsetOf(offset).flatMap(root.leafAt)
    }

    def referenceAt(offset: Int): Option[ReferenceNode] = data.structure.flatMap { root =>
      root.offsetOf(offset).flatMap(root.referenceAt)
    }

    def identifierAt(offset: Int): Option[IdentifiedNode] = data.structure.flatMap { root =>
      root.offsetOf(offset).flatMap(root.identifierAt)
    }

    def connectedLeafsFor(offset: Int): Seq[Node] = {
      val targetNode = referenceAt(offset) collect {
        case ReferenceNodeTarget(node: IdentifiedNode) => node
      } orElse {
        identifierAt(offset)
      }
      val refs = data.structure.toList.flatMap { root =>
        root.elements.collect {
          case ref @ ReferenceNodeTarget(target) if targetNode.contains(target) => ref
        }
      }
      targetNode.flatMap(_.id).toList ::: refs.flatMap(_.source)
    }
  }

  private[controller] implicit class NodeExt(val node: Node) extends AnyVal {
    def offsetOf(i: Int): Option[Int] =
      if (node.span.touches(i)) Some(i - node.span.begin) else None
  }

  private[controller] implicit class TerminalExt(val terminal: Terminal) extends AnyVal {
    def currentLineIntervalIn(document: Document): Interval = {
      val line    = document.lineNumberOf(terminal.offset)
      val begin   = document.startOffsetOf(line)
      val postfix = 1.min(document.linesCount - line - 1)
      val end     = document.endOffsetOf(line) + postfix
      Interval(begin, end)
    }

    def insertInto(document: Document, s: String): Unit =
      if (terminal.selection.isDefined) {
        val sel = terminal.selection.get
        terminal.selection = None
        val shift = sel.begin + s.length - terminal.offset
        if (shift < 0) terminal.offset += shift
        document.replace(sel, s)
        if (shift > 0) terminal.offset += shift
      } else {
        document.insert(terminal.offset, s)
        terminal.offset += s.length
      }
  }
}