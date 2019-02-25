/*
 *  FlashHolder.scala
 *  (Dotterweide)
 *
 *  Copyright (c) 2019 the Dotterweide authors. All rights reserved.
 *
 *  This software is published under the GNU Lesser General Public License v2.1+
 *
 *  For further information, please contact Hanns Holger Rutz at
 *  contact@sciss.de
 */

package dotterweide.editor

import dotterweide.{Interval, ObservableEvents}

class FlashEmitter extends ObservableEvents[FlashEmitted] {
  def emit(f: FlashEmitted): Unit = notifyObservers(f)
}

case class FlashEmitted(interval: Interval, isError: Boolean)