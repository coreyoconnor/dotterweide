/*
 *  UnindentSelectionTest.scala
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

package dotterweide.editor.controller

import org.junit.Test

class UnindentSelectionTest extends ActionTestBase(new UnindentSelection(_, _, 2)) {
  @Test
  def singleLine(): Unit = {
    assertEffectIs("[  foo|]", "[foo|]")
    assertEffectIs("[  foo]ba|r", "[foo]ba|r")
    assertEffectIs("[ foo|]", "[foo|]")
    assertEffectIs("[   foo|]", "[ foo|]")
    assertEffectIs("[foo|]", "[foo|]")
  }

  @Test
  def multipleLines(): Unit = {
    assertEffectIs("[  foo\n  bar|]", "[foo\nbar|]")
  }

  @Test
  def tailLine(): Unit = {
    assertEffectIs("[  foo\n  bar\n]  mo|o", "[foo\nbar\n]  mo|o")
  }
}