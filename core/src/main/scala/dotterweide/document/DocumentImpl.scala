/*
 *  DocumentImpl.scala
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

package dotterweide.document

class DocumentImpl(s: String = "") extends Document {
  private var ls = new LinedString(s)

  private var anchors = List[AnchorImpl]()

  def length: Int = ls.length

  def text: String = ls.toString

  def text_=(s: String): Unit =
    replace(0, length, s)

  def characters: CharSequence = ls

  def insert(offset: Int, s: String): Unit = {
    check(offset)
    ls = ls.replace(offset, offset, s)
    updateAnchors(offset, offset, offset + s.length)
    notifyObservers(Insertion(offset, s))
  }

  def remove(begin: Int, end: Int): Unit = {
    check(begin, end)
    val previous = ls.subSequence(begin, end)
    ls = ls.replace(begin, end, "")
    updateAnchors(begin, end, begin)
    notifyObservers(Removal(begin, end, previous))
  }

  def replace(begin: Int, end: Int, s: String): Unit = {
    check(begin, end)
    val previous = ls.subSequence(begin, end)
    ls = ls.replace(begin, end, s)
    updateAnchors(begin, end, begin + s.length)
    notifyObservers(Replacement(begin, end, previous, s))
  }

  private def updateAnchors(begin: Int, end: Int, end2: Int): Unit = {
    anchors.foreach(_.update(begin, end, end2))
  }

  private def check(offset: Int, parameter: String = "Offset"): Unit =
    if (offset < 0 || offset > length)
      throw new IndexOutOfBoundsException("%s (%d) must be withing [%d; %d]".format(parameter, offset, 0, length))

  private def check(begin: Int, end: Int): Unit = {
    check(begin, "Begin")
    check(end, "End")
    if (begin > end)
      throw new IllegalArgumentException("Begin (%d) must be not greater than end (%d)".format(begin, end))
  }

  def createAnchorAt(offset: Int, bias: Bias): Anchor = {
    val anchor = new AnchorImpl(offset, bias)
    anchors ::= anchor
    anchor
  }

  protected def wraps: Seq[Int] = ls.wraps

  private class AnchorImpl(var offset: Int, bias: Bias) extends Anchor {
    def dispose(): Unit =
      anchors = anchors.diff(Seq(this))

    def update(begin: Int, end: Int, end2: Int): Unit =
      if (begin < offset && end <= offset) {
        offset += end2 - end
      } else if ((begin < offset && offset < end && end2 < offset) ||
        (begin == end && begin == offset && bias == Bias.Right)) {
        offset = end2
      }
  }
}
