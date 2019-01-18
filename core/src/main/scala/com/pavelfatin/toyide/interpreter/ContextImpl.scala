/*
 *  ContextImpl.scala
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

package com.pavelfatin.toyide.interpreter

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

class ContextImpl extends Context {
  private val MaxFrames   = 100
  private var frames      = List[Frame]()
  private var allocations = List[ListBuffer[String]]()
  private val heap        = mutable.Map[String, Value]()

  def get(local: Boolean, name: String): Value =
    storage(local).getOrElse(name,
      throw new IllegalStateException(
        "%s value not found: %s".format(place(local), name)))

  def put(local: Boolean, name: String, value: Value): Unit = {
    storage(local).put(name, value) match {
      case Some(v) =>
        throw new IllegalStateException(
          "%s value %s is already exists: %s".format(place(local), name, v.presentation))
      case None => // ok
    }
    val scope = allocations.headOption.getOrElse(throw new IllegalStateException(
      "%s value %s (%s) allocation without a scope".format(place(local), name, value.presentation)))
    scope.append(name)
  }

  def update(local: Boolean, name: String, value: Value): Unit =
    storage(local).put(name, value) match {
      case Some(previous) =>
        if (previous.valueType != value.valueType)
          throw new IllegalStateException(
            "Type mismatch, expected %s, actual: %s".format(previous.valueType, value.valueType))
      case None =>
        throw new IllegalStateException(
          "%s value not found: %s".format(place(local), name))
    }

  private def place(local: Boolean) = if (local) "Frame" else "Heap"

  private def storage(local: Boolean) =
    if (local)
      frames.headOption.map(_.values).getOrElse(throw new IllegalStateException(
        "No active frame"))
    else
      heap

  def inScope(action: => Unit): Unit = {
    allocations ::= ListBuffer()
    try {
      action
      clearAllocations()
    } catch {
      case e: ReturnException =>
        clearAllocations()
        throw e
    }
  }

  private def clearAllocations(): Unit = {
    val storage = frames.headOption.fold(heap)(_.values)
    for (name <- allocations.head) {
      storage.remove(name) match {
        case Some(_) => // ok
        case None =>
          throw new IllegalStateException(
            "Allocated value %s not found".format(name))
      }
    }
    allocations = allocations.tail
  }

  def inFrame(place: Place)(action: => Unit): Option[Value] = {
    if (frames.size >= MaxFrames)
      throw new IllegalStateException("Stack overflow")

    frames ::= Frame(place)
    val result = try {
      action
      None
    } catch {
      case ReturnException(value) => value
    }
    frames = frames.tail
    result
  }

  def dropFrame(value: Option[Value]): Unit = {
    if (frames.isEmpty)
      throw new IllegalStateException("No frame to drop")

    throw ReturnException(value)
  }

  def trace: Seq[Place] = frames.map(_.place)

  private case class Frame(place: Place, values: mutable.Map[String, Value] = mutable.Map())

  private case class ReturnException(value: Option[Value]) extends RuntimeException
}