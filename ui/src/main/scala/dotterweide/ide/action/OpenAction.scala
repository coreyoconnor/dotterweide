/*
 *  OpenAction.scala
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

import dotterweide.ide.Panel
import javax.swing.KeyStroke
import javax.swing.filechooser.FileNameExtensionFilter

import scala.swing.{Action, Component, FileChooser}

class OpenAction(title0: String, mnemonic0: Char, shortcut: String,
                 parent: Component, panel: Panel) extends Action(title0) {
  mnemonic = mnemonic0

  accelerator = Some(KeyStroke.getKeyStroke(shortcut))

  def apply(): Unit = {
    val chooser = new FileChooser()
    chooser.title = "Open"
    chooser.fileFilter = new FileNameExtensionFilter(panel.fileType.name, panel.fileType.extension)
    chooser.showOpenDialog(parent) match {
      case FileChooser.Result.Approve =>
        val file = chooser.selectedFile
        panel.text = IO.read(file)
        panel.file = Some(file)
      case _ =>
    }
  }
}