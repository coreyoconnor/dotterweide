/*
 *  InvokeAction.scala
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

package dotterweide.ide.action

import dotterweide.editor.{Async, Data, Runner, StructureAction}
import dotterweide.ide.{Console, Launcher}
import dotterweide.node.Node
import javax.swing.KeyStroke

import scala.swing.Action

class InvokeAction(title0: String, mnemonic0: Char, shortcut: String, val data: Data,
                   invoker: Runner, launcher: Launcher, console: Console)(implicit val async: Async)
  extends Action(title0) with StructureAction {

  mnemonic = mnemonic0

  accelerator = Some(KeyStroke.getKeyStroke(shortcut))

  launcher.onChange {
    enabled = !launcher.active
  }

  def applyWithStructure(root: Node): Unit =
    if (!data.hasFatalErrors) {
      launcher.launch(invoker.run(root))
    } else {
      ErrorPrinter.print(data, console)
    }
}