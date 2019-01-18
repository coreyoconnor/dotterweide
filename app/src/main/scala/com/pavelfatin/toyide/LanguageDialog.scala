/*
 *  LanguageDialog.scala
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

package com.pavelfatin.toyide

import java.awt.event.{KeyEvent, MouseAdapter, MouseEvent}
import javax.swing.table.AbstractTableModel
import javax.swing.{JComponent, JTable, KeyStroke, ListSelectionModel}

import scala.swing.event.WindowClosing
import scala.swing.{Action, BorderPanel, Button, Component, Dialog, Dimension, FlowPanel, ScrollPane, Swing}

class LanguageDialog(languages: Seq[Language]) extends Dialog {
  private val table         = new JTable(LanguageTableModel)
  private val okButton      = new Button(Action("OK")(onOk()))
  private val cancelButton  = new Button(Action("Cancel")(onCancel()))

  private var itemSelected  = false

  modal         = true
  title         = "Language selection - Dotterweide"
  defaultButton = okButton
  preferredSize = new Dimension(350, 250)

  table.getColumnModel.getColumn(0).setMaxWidth(100)
  table.getSelectionModel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION)
  table.getSelectionModel.setSelectionInterval(0, 0)

  table.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)
    .put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "none")

  table.addMouseListener(new MouseAdapter {
    override def mouseClicked(e: MouseEvent): Unit = {
      if (e.getClickCount == 2) onOk()
    }
  })

  okButton.preferredSize = cancelButton.preferredSize

  peer.getRootPane.registerKeyboardAction(cancelButton.action.peer,
    KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW)

  reactions += {
    case WindowClosing(_) => onCancel()
  }

  contents = new BorderPanel() {
    border = Swing.EmptyBorder(10)
    layoutManager.setVgap(3)

    private val contentPane = new ScrollPane(Component.wrap(table))

    private val buttonsPane = new FlowPanel(FlowPanel.Alignment.Trailing)(
      okButton, Swing.HStrut(6), cancelButton) { hGap = 0; vGap = 0 }

    add(contentPane, BorderPanel.Position.Center)
    add(buttonsPane, BorderPanel.Position.South)
  }

  def selection: Option[Language] =
    if (itemSelected) Some(languages(table.getSelectedRow)) else None

  private def onOk(): Unit = {
    itemSelected = true
    dispose()
  }

  private def onCancel(): Unit =
    dispose()

  private object LanguageTableModel extends AbstractTableModel {
    def getRowCount   : Int = languages.length
    def getColumnCount: Int = 2

    override def getColumnName(column: Int): String = column match {
      case 0 => "Name"
      case 1 => "Description"
    }

    def getValueAt(rowIndex: Int, columnIndex: Int): String = {
      val language = languages(rowIndex)

      columnIndex match {
        case 0 => language.name
        case 1 => language.description
      }
    }
  }
}
