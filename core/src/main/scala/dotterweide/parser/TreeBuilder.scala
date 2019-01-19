/*
 *  TreeBuilder.scala
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

package dotterweide.parser

import dotterweide.Span
import dotterweide.lexer.{Token, TokenKind}
import dotterweide.node.NodeImpl

import scala.collection.immutable.{Seq => ISeq}

class TreeBuilder(input: Iterator[Token]) {
  private val in = input.filterNot(_.kind == TokenKind.WS).buffered

  private var head: Option[Token] = None

  private var headSpan = Span("", 0, 0)

  private var regions = List(new MyRegion(headSpan))

  if (hasNext) {
    advance()
  }

  def ahead(kinds: TokenKind*): Boolean =
    in.hasNext && kinds.contains(in.head.kind)

  def matches(kinds: TokenKind*): Boolean =
    head.exists(token => kinds.contains(token.kind))

  def consume(kinds: TokenKind*): Unit =
    if (matches(kinds: _*))
      consume()
    else
      error("Expected %s".format(kinds.map(_.name).mkString(", ")))

  def consume(): Unit = {
    val token = head.getOrElse(throw new NoSuchTokenException())
    if (regions.tail.isEmpty) throw new ConsumeWithoutRegionException()
    regions.head.add(NodeImpl.createLeaf(token))
    advance()
  }

  def grasp(kinds: TokenKind*): Boolean = {
    val matched = matches(kinds: _*)
    if (matched) consume()
    matched
  }

  def error(message: String): Unit = {
    val region = regions.head

    if (region.hasProblem) return

    if (isEOF) {
      val span = Span(headSpan.source, headSpan.end, headSpan.end)
      region.add(NodeImpl.createError(None, span, message))
    } else {
      val token = head.getOrElse(throw new NoSuchTokenException())
      region.add(NodeImpl.createError(Some(token), token.span, message))
//      advance()
    }
  }

  def isEOF: Boolean = !hasNext && head.isEmpty

  private def hasNext = in.hasNext

  def advance(): Unit = {
    head = if (hasNext) Some(in.next()) else None
    head.foreach { token =>
      headSpan = token.span
    }
  }

  def tree: NodeImpl = {
    if (regions.tail.nonEmpty) throw new UnclosedRegionException
    val nodes = regions.head.nodes
    val root  = nodes.headOption.getOrElse(throw new NoRootNodeException)
    if (nodes.tail.nonEmpty) throw new MultipleRootNodesException()
    root
  }

  def open(): Region = {
    val region = new MyRegion(headSpan)
    regions ::= region
    region
  }

  def capturing(node: NodeImpl, collapseHolderNode: Boolean = false)(action: => Unit): Unit = {
    val region = open()
    action
    region.close(node, collapseHolderNode)
  }

  def folding(node: => NodeImpl, collapseHolderNode: Boolean = false, length: Int = 3)(action: => Unit): Unit = {
    val region = open()
    action
    region.fold(node, collapseHolderNode, length)
  }

  private class MyRegion(tokenSpan: Span) extends Region {
    private var entries = List.empty[NodeImpl]
    private var closed  = false

    def close(node: NodeImpl, collapseHolderNode: Boolean): Unit =
      capture(node, collapseHolderNode = collapseHolderNode)(identity)

    def fold(node: => NodeImpl, collapseHolderNode: Boolean, length: Int): Unit =
      capture(node, collapseHolderNode = collapseHolderNode) {
        case Nil            => Nil
        case _head :: Nil   => _head :: Nil
        case _head :: tail  =>
          val root = tail.grouped(length - 1).foldLeft(_head) { (left, part) =>
            val parent = node // call-by-name for factory method
            parent.children = left +: part
            parent
          }
          root.children
      }

    private def capture(node: NodeImpl, collapseHolderNode: Boolean)(f: ISeq[NodeImpl] => ISeq[NodeImpl]): Unit = {
      if (closed) throw new MultipleClosingException
      if (!this.eq(regions.head)) throw new IncorrectRegionsOrderException
      regions = regions.tail
      val children = if (collapseHolderNode && nodes.size == 1) nodes.head else {
        if (entries.isEmpty)
          node.span = tokenSpan.leftEdge
        else
          node.children = f(nodes)
        node
      }
      regions.head.add(children)
      closed = true
    }

    def add(node: NodeImpl): Unit = {
      if (closed) throw new RuntimeException("Unable to add node to closed region")
      entries ::= node
    }

    def hasProblem: Boolean = entries.exists(_.elements.exists(_.problem.isDefined))

    def nodes: List[NodeImpl] = entries.reverse
  }
}

trait Region {
  def close(node:    NodeImpl, collapseHolderNode: Boolean = false): Unit
  def fold (node: => NodeImpl, collapseHolderNode: Boolean = false, length: Int = 3): Unit
}

class NoRootNodeException             extends RuntimeException
class MultipleRootNodesException      extends RuntimeException
class NoSuchTokenException            extends RuntimeException
class ConsumeWithoutRegionException   extends RuntimeException
class UnclosedRegionException         extends RuntimeException
class MultipleClosingException        extends RuntimeException
class IncorrectRegionsOrderException  extends RuntimeException