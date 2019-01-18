/*
 *  HistoryImplTest.scala
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

import org.junit.Test
import org.junit.Assert._
import dotterweide.Helpers._
import dotterweide.document.Document
import dotterweide.Interval

class HistoryImplTest {
  @Test
  def initialState(): Unit = {
    val history = new HistoryImpl()
    assertFalse(history.canUndo)
    assertFalse(history.canRedo)
  }

  @Test(expected = classOf[IllegalStateException])
  def illegalUndo(): Unit = {
    new HistoryImpl().undo()
  }

  @Test(expected = classOf[IllegalStateException])
  def illegalRedo(): Unit = {
    new HistoryImpl().undo()
  }

  @Test(expected = classOf[IllegalStateException])
  def nestedRecording(): Unit = {
    val history = new HistoryImpl()
    val (document, terminal) = parseDocument("|")
    history.capture(document, terminal) {
      history.capture(document, terminal) {}
    }
  }

  def caretMovementsAreNotRecorded(): Unit = {
    val history = new HistoryImpl()
    val (document, terminal) = parseDocument("|foo")
    history.capture(document, terminal) {
      terminal.offset = 1
    }
    assertFalse(history.canUndo)
  }

  @Test
  def insert(): Unit = {
    assertEffectsAre("|", "|foo") { (document, _) =>
      document.insert(0, "foo")
    }
    assertEffectsAre("|fooMoo", "|fooBarMoo") { (document, _) =>
      document.insert(3, "Bar")
    }
  }

  @Test
  def remove(): Unit = {
    assertEffectsAre("|foo", "|") { (document, _) =>
      document.remove(0, 3)
    }
    assertEffectsAre("|fooBarMoo", "|fooMoo") { (document, _) =>
      document.remove(3, 6)
    }
  }

  @Test
  def replace(): Unit = {
    assertEffectsAre("|foo", "|bar") { (document, _) =>
      document.replace(0, 3, "bar")
    }
    assertEffectsAre("|fooBarMoo", "|fooGooMoo") { (document, _) =>
      document.replace(3, 6, "Goo")
    }
  }

  @Test
  def caret(): Unit = {
    assertEffectsAre("|", "foo|") { (document, terminal) =>
      document.insert(0, "foo")
      terminal.offset += 3
    }
    assertEffectsAre("foo|", "|") { (document, terminal) =>
      terminal.offset -= 3
      document.remove(0, 3)
    }
    assertEffectsAre("|foo", "bar|") { (document, terminal) =>
      document.replace(0, 3, "bar")
      terminal.offset += 3
    }
  }

  @Test
  def selection(): Unit = {
    assertEffectsAre("|", "[|foo]") { (document, terminal) =>
      document.insert(0, "foo")
      terminal.selection = Some(Interval(0, 3))
    }
    assertEffectsAre("[|foo]", "|") { (document, terminal) =>
      terminal.selection = None
      document.remove(0, 3)
    }
    assertEffectsAre("[|foo]", "|bar") { (document, terminal) =>
      document.replace(0, 3, "bar")
      terminal.selection = None
    }
    assertEffectsAre("|foo", "[|bar]") { (document, terminal) =>
      document.replace(0, 3, "bar")
      terminal.selection = Some(Interval(0, 3))
    }
    assertEffectsAre("[|foo]", "|b[ar]") { (document, terminal) =>
      document.replace(0, 3, "bar")
      terminal.selection = Some(Interval(1, 3))
    }
  }

  @Test
  def sequence(): Unit = {
    assertEffectsAre("|", "Q|ED[ob]r") { (document, terminal) =>
      document.insert(0, "bar")
      document.insert(0, "foo")
      terminal.offset = 3
      terminal.selection = Some(Interval(1, 3))
      document.remove(1, 2)
      document.remove(3, 4)
      document.replace(0, 1, "QED")
      terminal.offset = 1
      terminal.selection = Some(Interval(3, 5))
    }
  }

  @Test
  def sequenceOrder(): Unit = {
    assertEffectsAre("|", "|") { (document, _) =>
      document.insert(0, "foo")
      document.remove(0, 3)
    }
    assertEffectsAre("|foo", "|foo") { (document, _) =>
      document.remove(0, 3)
      document.insert(0, "foo")
    }
  }

  @Test
  def undoRedo(): Unit = {
    val history = new HistoryImpl()

    val (document, terminal) = parseDocument("|")

    history.capture(document, terminal)(document.insert(0, "a"))
    assertTrue(history.canUndo)
    assertFalse(history.canRedo)
    assertEquals("|a", formatDocument(document, terminal))

    history.capture(document, terminal)(document.insert(1, "b"))
    assertTrue(history.canUndo)
    assertFalse(history.canRedo)
    assertEquals("|ab", formatDocument(document, terminal))

    history.undo()
    assertTrue(history.canUndo)
    assertTrue(history.canRedo)
    assertEquals("|a", formatDocument(document, terminal))

    history.redo()
    assertTrue(history.canUndo)
    assertFalse(history.canRedo)
    assertEquals("|ab", formatDocument(document, terminal))

    history.undo()
    assertTrue(history.canUndo)
    assertTrue(history.canRedo)
    assertEquals("|a", formatDocument(document, terminal))

    history.undo()
    assertFalse(history.canUndo)
    assertTrue(history.canRedo)
    assertEquals("|", formatDocument(document, terminal))
  }

  @Test
  def recordingClearsRedo(): Unit = {
    val history = new HistoryImpl()

    val (document, terminal) = parseDocument("|")

    history.capture(document, terminal)(document.insert(0, "a"))
    history.capture(document, terminal)(document.insert(1, "b"))
    history.undo()

    history.capture(document, terminal)(document.insert(1, "c"))
    assertTrue(history.canUndo)
    assertFalse(history.canRedo)
    assertEquals("|ac", formatDocument(document, terminal))

    history.undo()
    assertTrue(history.canUndo)
    assertTrue(history.canRedo)
    assertEquals("|a", formatDocument(document, terminal))

    history.redo()
    assertTrue(history.canUndo)
    assertFalse(history.canRedo)
    assertEquals("|ac", formatDocument(document, terminal))
  }

  protected def assertEffectsAre(before: String, after: String)(action: (Document, Terminal) => Unit): Unit = {
    val (document, terminal) = parseDocument(before)

    val history = new HistoryImpl()

    history.capture(document, terminal) {
      action(document, terminal)
    }

    assertEquals(after, formatDocument(document, terminal)) // assert that action produces expected changes

    history.undo()
    assertEquals(before, formatDocument(document, terminal))

    history.redo()
    assertEquals(after, formatDocument(document, terminal))
  }
}