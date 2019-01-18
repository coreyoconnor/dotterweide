/*
 *  LinedStringTest.scala
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

package com.pavelfatin.toyide.document

import org.junit.Test
import org.junit.Assert._

class LinedStringTest {
  @Test
  def construction(): Unit = {
    assertEquals(List(""), ls("").lines)
    assertEquals(List("foo"), ls("foo").lines)
    assertEquals(List("foo\n", ""), ls("foo\n").lines)
    assertEquals(List("foo\n", "bar"), ls("foo\nbar").lines)
    assertEquals(List("foo\n", "bar\n", ""), ls("foo\nbar\n").lines)
    assertEquals(List("foo\n", "bar\n", "moo"), ls("foo\nbar\nmoo").lines)
    assertEquals(List("foo\n","bar\n", "moo\n", ""), ls("foo\nbar\nmoo\n").lines)
    assertEquals(List("\n", ""), ls("\n").lines)
    assertEquals(List("\n", "\n", ""), ls("\n\n").lines)
    assertEquals(List("\n", "\n", "\n", ""), ls("\n\n\n").lines)
  }
  
  @Test
  def length(): Unit = {
    assertEquals(0, ls("").length)
    assertEquals(3, ls("foo").length)
    assertEquals(6, ls("foo\nam").length)
    assertEquals(10, ls("foo\nbar\nam").length)
  }

  @Test
  def charAt(): Unit = {
    assertEquals('a', ls("a").charAt(0))
    assertEquals('a', ls("ab").charAt(0))
    assertEquals('b', ls("ab").charAt(1))
    assertEquals('a', ls("abc").charAt(0))
    assertEquals('b', ls("abc").charAt(1))
    assertEquals('c', ls("abc").charAt(2))
    assertEquals('a', ls("a\nb\nc").charAt(0))
    assertEquals('\n', ls("a\nb\nc").charAt(1))
    assertEquals('b', ls("a\nb\nc").charAt(2))
    assertEquals('\n', ls("a\nb\nc").charAt(3))
    assertEquals('c', ls("a\nb\nc").charAt(4))
    assertEquals('b', ls("foo\nbar\ngoo").charAt(4))
    assertEquals('r', ls("foo\nbar\ngoo").charAt(6))
    assertEquals('g', ls("foo\nbar\ngoo").charAt(8))
  }

  @Test
  def asString(): Unit = {
    assertEquals("", ls("").toString)
    assertEquals("\n", ls("\n").toString)
    assertEquals("\n\n", ls("\n\n").toString)
    assertEquals("\n\n\n", ls("\n\n\n").toString)
    assertEquals("foo", ls("foo").toString)
    assertEquals("foo\n", ls("foo\n").toString)
    assertEquals("foo\nbar", ls("foo\nbar").toString)
    assertEquals("foo\nbar\n", ls("foo\nbar\n").toString)
    assertEquals("foo\nbar\nmoo", ls("foo\nbar\nmoo").toString)
    assertEquals("foo\nbar\nmoo\n", ls("foo\nbar\nmoo\n").toString)
    assertEquals("foo\nbar\nmoo\n\n", ls("foo\nbar\nmoo\n\n").toString)
  }

  @Test(expected = classOf[IndexOutOfBoundsException])
  def charAtNegativeIndex(): Unit = {
    ls("").charAt(-1)
  }

  @Test(expected = classOf[IndexOutOfBoundsException])
  def charAtGreaterIndex(): Unit = {
    ls("foo").charAt(3)
  }

  @Test(expected = classOf[IndexOutOfBoundsException])
  def charAtGreaterIndexChained(): Unit = {
    ls("foo\nbar").charAt(7)
  }

  @Test
  def subSequence(): Unit = {
    assertEquals(List(""), ls("").subSequence(0, 0).lines)
    assertEquals(List("a"), ls("a").subSequence(0, 1).lines)
    assertEquals(List("foo"), ls("foo").subSequence(0, 3).lines)

    assertEquals(List(""), ls("foo").subSequence(0, 0).lines)
    assertEquals(List("f"), ls("foo").subSequence(0, 1).lines)
    assertEquals(List("fo"), ls("foo").subSequence(0, 2).lines)
    assertEquals(List("oo"), ls("foo").subSequence(1, 3).lines)

    assertEquals(List(""), ls("\n").subSequence(0, 0).lines)
    assertEquals(List("\n", ""), ls("\n").subSequence(0, 1).lines)
    assertEquals(List("\n", ""), ls("\n\n").subSequence(0, 1).lines)
    assertEquals(List("\n", "\n", ""), ls("\n\n").subSequence(0, 2).lines)

    assertEquals(List(""), ls("foo\nbar").subSequence(4, 4).lines)
    assertEquals(List("b"), ls("foo\nbar").subSequence(4, 5).lines)
    assertEquals(List("ba"), ls("foo\nbar").subSequence(4, 6).lines)
    assertEquals(List("ar"), ls("foo\nbar").subSequence(5, 7).lines)

    assertEquals(List("foo\n", "bar"), ls("foo\nbar").subSequence(0, 7).lines)
    assertEquals(List("foo\n", ""), ls("foo\nbar").subSequence(0, 4).lines)
    assertEquals(List("\n", "bar"), ls("foo\nbar").subSequence(3, 7).lines)

    assertEquals(List("foo\n", "bar\n", "goo"), ls("foo\nbar\ngoo").subSequence(0, 11).lines)
    assertEquals(List("oo\n", "bar\n", "goo"), ls("foo\nbar\ngoo").subSequence(1, 11).lines)
    assertEquals(List("foo\n", "bar\n", "go"), ls("foo\nbar\ngoo").subSequence(0, 10).lines)
  }

  @Test(expected = classOf[IndexOutOfBoundsException])
  def subSequenceNegativeStart(): Unit = {
    ls("").subSequence(-1, 0)
  }

  @Test(expected = classOf[IndexOutOfBoundsException])
  def subSequenceNegativeEnd(): Unit = {
    ls("").subSequence(0, -1)
  }

  @Test(expected = classOf[IndexOutOfBoundsException])
  def subSequenceGreaterStart(): Unit = {
    ls("foo").subSequence(4, 4)
  }

  @Test(expected = classOf[IndexOutOfBoundsException])
  def subSequenceGreaterEnd(): Unit = {
    ls("foo").subSequence(3, 4)
  }

  @Test(expected = classOf[IndexOutOfBoundsException])
  def subSequenceGreaterOnEmpty(): Unit = {
    ls("").subSequence(0, 1)
  }

  @Test(expected = classOf[IllegalArgumentException])
  def subSequenceNegativeInterval(): Unit = {
    ls("foo").subSequence(3, 2)
  }

   @Test
  def concat(): Unit = {
    assertEquals(List(""), ls("").concat(ls("")).lines)

    assertEquals(List("foo"), ls("foo").concat(ls("")).lines)
    assertEquals(List("foo\n", ""), ls("foo\n").concat(ls("")).lines)

    assertEquals(List("foo\n", "bar"), ls("foo\nbar").concat(ls("")).lines)
    assertEquals(List("foo\n", "bar\n", ""), ls("foo\nbar\n").concat(ls("")).lines)

    assertEquals(List("foo"), ls("").concat(ls("foo")).lines)
    assertEquals(List("foo\n", ""), ls("").concat(ls("foo\n")).lines)

    assertEquals(List("foo\n", "bar"), ls("").concat(ls("foo\nbar")).lines)
    assertEquals(List("foo\n", "bar\n", ""), ls("").concat(ls("foo\nbar\n")).lines)

    assertEquals(List("foobar"), ls("foo").concat(ls("bar")).lines)
    assertEquals(List("foobar\n", ""), ls("foo").concat(ls("bar\n")).lines)
    assertEquals(List("foo\n", "bar"), ls("foo\n").concat(ls("bar")).lines)
    assertEquals(List("foo\n", "bar\n", ""), ls("foo\n").concat(ls("bar\n")).lines)

    assertEquals(List("foo\n", ""), ls("foo").concat(ls("\n")).lines)
    assertEquals(List("\n", "foo"), ls("\n").concat(ls("foo")).lines)

    assertEquals(List("foo\n", "barmoo\n", "goo"), ls("foo\nbar").concat(ls("moo\ngoo")).lines)
    assertEquals(List("foo\n", "bar\n", "moo\n", "goo"), ls("foo\nbar\n").concat(ls("moo\ngoo")).lines)
  }

  @Test
  def replace(): Unit = {
    assertEquals(List("foo"), ls("").replace(0, 0, "foo").lines)
    assertEquals(List("bar"), ls("foo").replace(0, 3, "bar").lines)
    assertEquals(List(""), ls("foo").replace(0, 3, "").lines)
    assertEquals(List("moo"), ls("foo").replace(0, 1, "m").lines)

    assertEquals(List("foo\n", "bmoo\n", "goor"), ls("foo\nbar").replace(5, 6, "moo\ngoo").lines)

    assertEquals(List("foo\n", ""), ls("\n").replace(0, 0, "foo").lines)
    assertEquals(List("\n", "foo"), ls("\n").replace(1, 1, "foo").lines)
  }

  @Test
  def wraps(): Unit = {
    assertEquals(List(), ls("").wraps)
    assertEquals(List(0), ls("\n").wraps)
    assertEquals(List(0, 1), ls("\n\n").wraps)
    assertEquals(List(), ls("foo").wraps)
    assertEquals(List(3), ls("foo\n").wraps)
    assertEquals(List(3), ls("foo\nbar").wraps)
    assertEquals(List(3, 7), ls("foo\nbar\nmoo").wraps)
    assertEquals(List(3, 7, 12), ls("foo\nbar\nmooo\ngoo").wraps)
  }

  @Test
  def immutability(): Unit = {
    val original = ls("foo")
    original.replace(0, 3, "bar")
    assertEquals(List("foo"), original.lines)
  }

  @Test
  def linesIdentity(): Unit = {
    val original = ls("foo\nbar\nmoo")
    val replaced = original.replace(4, 7, "goo")
    assertTrue(original.lines(0).eq(replaced.lines(0)))
    assertTrue(original.lines(2).eq(replaced.lines(2)))
  }

  private def ls(s: String): LinedString = new LinedString(s)
}