/*
 *  AbstractPainter.scala
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

package dotterweide.editor.painter

import java.awt.{Graphics, Point, Rectangle}

import dotterweide.Interval
import dotterweide.document.Document
import dotterweide.editor.{Area, Canvas, Coloring, Data, Grid, Terminal}

private abstract class AbstractPainter(context: PainterContext) extends Painter {
  protected def document: Document  = context.document
  protected def terminal: Terminal  = context.terminal
  protected def data    : Data      = context.data
  protected def canvas  : Canvas    = context.canvas
  protected def grid    : Grid      = context.grid
  protected def coloring: Coloring  = context.coloring

  protected def contains(chars: CharSequence, char: Char): Boolean =
    Range(0, chars.length).exists(i => chars.charAt(i) == char)

  protected def fill(g: Graphics, r: Rectangle): Unit =
    g.fillRect(r.x, r.y, r.width, r.height)

  protected def toPoint(offset: Int): Point =
    grid.toPoint(document.toLocation(offset))

  protected def notifyObservers(interval: Interval): Unit =
    rectanglesOf(interval).foreach(notifyObservers)

  protected def lineRectangleAt(offset: Int): Rectangle = {
    val point = toPoint(offset)
    new Rectangle(0, point.y, canvas.size.width, grid.cellSize.height)
  }

  protected def caretRectangleAt(offset: Int): Rectangle = {
    val point = toPoint(offset)
    new Rectangle(point.x, point.y, 2, grid.cellSize.height)
  }

  protected def rectanglesOf(interval: Interval): Seq[Rectangle] = {
    val width   = canvas.size.width
    val height  = grid.cellSize.height

    val p1      = toPoint(interval.begin)
    val p2      = toPoint(interval.end)

    if (p1.y == p2.y) {
      Seq(new Rectangle(p1.x, p1.y, p2.x - p1.x, height))
    } else {
      Seq(
        new Rectangle(p1.x, p1.y, width - p1.x, height),
        new Rectangle(grid.insets.left, p1.y + height, width - grid.insets.left, p2.y - p1.y - height),
        new Rectangle(grid.insets.left, p2.y, p2.x - grid.insets.left, height))
    }
  }

  protected def intervalOf(area: Area): Interval = {
    val beginLine = bound(area.line)
    val endLine   = bound(area.line + area.height)
    Interval(document.startOffsetOf(beginLine), document.endOffsetOf(endLine))
  }

  private def bound(line: Int): Int = 0.max(line.min(document.linesCount - 1))
}
