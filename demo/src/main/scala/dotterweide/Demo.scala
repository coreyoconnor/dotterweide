/*
 *  Application.scala
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

package dotterweide

import java.awt.Dimension
import java.util.Locale

import dotterweide.editor.ColorScheme
import dotterweide.ide.MainFrame
import dotterweide.languages.lisp.LispLanguage
import dotterweide.languages.scala.ScalaLanguage
import dotterweide.languages.toy.ToyLanguage

import scala.swing.event.WindowClosed
import scala.swing.{Swing, Window}

object Demo {
  private val Languages = List(new ScalaLanguage, ToyLanguage, LispLanguage)

  case class Config(language: Option[Language] = None, stylingName: Option[String] = None)

  def main(args: Array[String]): Unit = {
    val default = Config()

    def findLanguage(name: String): Option[Language] = {
      val n          = name.toLowerCase(Locale.US)
      Languages.find(_.name.toLowerCase(Locale.US) == n)
    }

    val p = new scopt.OptionParser[Config]("Demo") {
      opt[String]('l', "language")
        .text(s"Select language (one of ${Languages.map(_.name).mkString(", ")})")
        .validate { v => if (findLanguage(v).isDefined) success else failure(s"Unknown language $v") }
        .action { (v, c) => c.copy(language = findLanguage(v)) }

      opt[String]('c', "colors")
        .text(s"Select color scheme name (one of ${Languages.map(_.name).mkString(", ")})")
        .validate { v => if (ColorScheme.names.contains(v.capitalize)) success else failure(s"Unknown scheme $v") }
        .action { (v, c) => c.copy(stylingName = Some(v.capitalize)) }
    }
    p.parse(args, default).fold(sys.exit(1)) { config =>
      Swing.onEDT(run(config))
    }
  }

  def run(config: Config): Unit = {
    val langOpt = config.language.orElse(selectLanguage())
    langOpt.foreach { language =>
      val code  = language.examples.headOption.fold("")(_.code)
      val frame = new MainFrame(language, code, stylingName = config.stylingName)
      frame.preferredSize = new Dimension(874, 696)
      open(frame)
      frame.listenTo(frame)
      frame.reactions += {
        case WindowClosed(_) => sys.exit()
      }
    }
  }

  private def selectLanguage(): Option[Language] = {
    val dialog = new LanguageDialog(Languages)
    open(dialog)
    dialog.selection
  }

  private def open(window: Window): Unit = {
    window.pack()
    window.centerOnScreen()
    window.open()
  }
}
