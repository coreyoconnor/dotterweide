/*
 *  ControllerImplTest.scala
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

package com.pavelfatin.toyide.editor.controller

import java.awt.{Insets, Dimension}

import org.junit.Test
import org.junit.Assert._
import com.pavelfatin.toyide.Helpers._
import com.pavelfatin.toyide.document.Document
import com.pavelfatin.toyide.editor._
import com.pavelfatin.toyide.formatter.FormatterImpl

class ControllerImplTest {
  @Test
  def enter(): Unit = {
    assertEffectIs("|", "\n|")(_.processEnterPressed())
    assertEffectIs("|foo", "\n|foo")(_.processEnterPressed())
    assertEffectIs("foo|", "foo\n|")(_.processEnterPressed())
    assertEffectIs("foo|bar", "foo\n|bar")(_.processEnterPressed())
  }

  @Test
  def enterWithCaretHold(): Unit = {
    assertEffectIs("foo|bar", "foo|\nbar")(_.processEnterPressed(true))
  }

  @Test
  def enterWithIndent(): Unit = {
    assertEffectIs("  foo|", "  foo\n  |")(_.processEnterPressed())
    assertEffectIs("  foo|bar", "  foo\n  |bar")(_.processEnterPressed())

    assertEffectIs("{\n  |", "{\n  \n  |")(_.processEnterPressed())
  }

  @Test
  def enterWithIndentIncrease(): Unit = {
    assertEffectIs("{|", "{\n  |")(_.processEnterPressed())
    assertEffectIs("  {|", "  {\n    |")(_.processEnterPressed())

    assertEffectIs("{ |", "{ \n  |")(_.processEnterPressed())
  }

  @Test
  def enterWithIndentDecrease(): Unit = {
    assertEffectIs("{|}", "{\n  |\n}")(_.processEnterPressed())
    assertEffectIs("  {|}", "  {\n    |\n  }")(_.processEnterPressed())

    assertEffectIs("{| }", "{\n | }")(_.processEnterPressed())
  }

  @Test
  def enterWithIndentPersists(): Unit = {
    assertEffectIs("|{", "\n|{")(_.processEnterPressed())
    assertEffectIs("  |{", "  \n  |{")(_.processEnterPressed())

    assertEffectIs("}|", "}\n|")(_.processEnterPressed())
    assertEffectIs("  }|", "  }\n  |")(_.processEnterPressed())

    assertEffectIs("{}|", "{}\n|")(_.processEnterPressed())
    assertEffectIs("  {}|", "  {}\n  |")(_.processEnterPressed())

    assertEffectIs("|{}", "\n|{}")(_.processEnterPressed())
    assertEffectIs("  |{}", "  \n  |{}")(_.processEnterPressed())

    assertEffectIs("{\n  |}", "{\n  \n  |}")(_.processEnterPressed())

    assertEffectIs("  foo\n|  bar", "  foo\n\n|  bar")(_.processEnterPressed())
    assertEffectIs("  foo\n | bar", "  foo\n \n | bar")(_.processEnterPressed())
    assertEffectIs("  foo\n  |bar", "  foo\n  \n  |bar")(_.processEnterPressed())
  }

  @Test
  def enterBeforePureIndent(): Unit = {
    assertEffectIs("|  ", "\n|  ")(_.processEnterPressed())
  }

  @Test
  def char(): Unit = {
    assertEffectIs("|", "a|")(_.processCharInsertion('a'))
    assertEffectIs("a|c", "ab|c")(_.processCharInsertion('b'))
  }

  @Test
  def charComplement(): Unit = {
    assertEffectIs("|", "(|)")(_.processCharInsertion('('))
    assertEffectIs("|", "[|]")(_.processCharInsertion('['))
    assertEffectIs("|", "{|}")(_.processCharInsertion('{'))
    assertEffectIs("|", "\"|\"")(_.processCharInsertion('"'))
  }

  @Test
  def charComplementSuppression(): Unit = {
    assertEffectIs("|a", "(|a")(_.processCharInsertion('('))
    assertEffectIs("|1", "(|1")(_.processCharInsertion('('))
    assertEffectIs("|(", "(|(")(_.processCharInsertion('('))
  }

  @Test
  def charComplementNonSuppression(): Unit = {
    assertEffectIs("|)", "[|])")(_.processCharInsertion('['))
    assertEffectIs("|)", "(|))")(_.processCharInsertion('('))
  }

  @Test
  def charComplementOverwrite(): Unit = {
    assertEffectIs("(|)", "()|")(_.processCharInsertion(')'))
  }

  @Test
  def charClosingMark(): Unit = {
    assertEffectIs("|", "}|")(_.processCharInsertion('}'))
  }

  @Test
  def charClosingMarkIndentDecrease(): Unit = {
    assertEffectIs("  foo\n  |", "  foo\n}|")(_.processCharInsertion('}'))
    assertEffectIs("    foo\n    |", "    foo\n  }|")(_.processCharInsertion('}'))
  }

  @Test
  def charClosingMarkIndentDecreaseAfterIncrease(): Unit = {
    assertEffectIs("  {\n  |", "  {\n  }|")(_.processCharInsertion('}'))
  }

  @Test
  def charClosingMarkIndentDecreaseBlocked(): Unit = {
    assertEffectIs("    foo\n   a|", "    foo\n   a}|")(_.processCharInsertion('}'))
  }

  @Test
  def charClosingMarkIndentIncrease(): Unit = {
    assertEffectIs("  foo\n|", "  foo\n}|")(_.processCharInsertion('}'))
    assertEffectIs("    foo\n|", "    foo\n  }|")(_.processCharInsertion('}'))
  }

  @Test
  def charClosingMarkIndentIncreaseBlocked(): Unit = {
    assertEffectIs("    foo\na|", "    foo\na}|")(_.processCharInsertion('}'))
  }

  protected def assertEffectIs(before: String, after: String)(f: ControllerImpl => Unit): Unit = {
    doAssertEffectIs(before, after) { (document, terminal) =>
      val GridMock = new Grid(new Dimension(8, 8), new Insets(0, 0, 0, 0))
      val controller = new ControllerImpl(document, new MockData(), terminal, GridMock, new MockAdviser(),
        new FormatterImpl(new MockFormat()), 2, "//", new HistoryImpl())
      f(controller)
    }
  }

  private def doAssertEffectIs(before: String, after: String)(block: (Document, Terminal) => Unit): Unit = {
    val (document, terminal) = parseDocument(before)
    block(document, terminal)
    val text = formatDocument(document, terminal)
    assertEquals(after, text)
  }
}