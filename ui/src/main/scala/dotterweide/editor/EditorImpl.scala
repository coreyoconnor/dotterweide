/*
 *  EditorImpl.scala
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

import java.awt.event.{ActionEvent, ActionListener, FocusEvent, FocusListener, KeyAdapter, KeyEvent, MouseAdapter, MouseEvent, MouseMotionAdapter}
import java.awt.font.FontRenderContext
import java.awt.{BorderLayout, Cursor, Dimension, Font, Graphics, Graphics2D, Point, Rectangle, Toolkit}

import dotterweide.Interval
import dotterweide.document.Document
import dotterweide.editor.controller.{Controller, ControllerImpl}
import dotterweide.editor.painter.{Painter, PainterFactory}
import dotterweide.formatter.{Format, FormatterImpl}
import dotterweide.lexer.Lexer
import javax.swing.border.EmptyBorder
import javax.swing.{JComponent, JPanel, JScrollPane, JViewport, KeyStroke, ListCellRenderer, Scrollable, SwingConstants, Timer}

import scala.collection.immutable.{Seq => ISeq}

private class EditorImpl(val document: Document, val data: Data, val holder: ErrorHolder,
                         lexer: Lexer, styling: Styling, font: FontSettings, matcher: BraceMatcher,
                         format: Format, adviser: Adviser, listRenderer: ListCellRenderer[AnyRef],
                         lineCommentPrefix: String, history: History)
                        (implicit val async: Async) extends Editor {

  private def mkFont() = new Font(font.family, Font.PLAIN, font.size)

  private var regularFont: Font = mkFont()

  private def gridParam(): (Int, Int, Int) = {
    val frc     = new FontRenderContext(null, true, false)
    val sb      = regularFont.getStringBounds("X", frc)
    val ascent  = math.ceil(-sb.getY).toInt
    val advance = sb.getWidth.toInt
    val height  = math.ceil(sb.getHeight * font.lineSpacing).toInt
    (advance, height, ascent)
  }

  private val grid = {
    val pIn = Pane.getInsets
    val (advance, height, ascent) = gridParam()
    new GridImpl(cellWidth0 = advance, cellHeight0 = height, ascent0 = ascent,
      insetLeft = pIn.left, insetTop = pIn.top, insetRight = pIn.right, insetBottom = pIn.bottom)
  }

  font.onChange {
    regularFont = mkFont()
    val (advance, height, ascent) = gridParam()
    grid.cellWidth  = advance
    grid.cellHeight = height
    grid.ascent     = ascent
    Pane.repaint()
  }

  private lazy val renderingHints = Option(Toolkit.getDefaultToolkit.getDesktopProperty("awt.font.desktophints"))

  private val controller: Controller =
    new ControllerImpl(document, data, terminal, grid, adviser,
      new FormatterImpl(format), tabSize = format.defaultTabSize, lineCommentPrefix = lineCommentPrefix,
      font = font, history = history)

  private val scroll = new JScrollPane(Pane)

  private val canvas = new CanvasImpl(Pane, scroll)

  val component: swing.Component = {
    val stripe = new Stripe(document, data, holder, grid, canvas)
    stripe.onChange { y =>
      val point = toPoint(terminal.offset)
      terminal.offset = document.toNearestOffset(grid.toLocation(new Point(point.x, y)))
      val h = Pane.getVisibleRect.height
      Pane.scrollRectToVisible(new Rectangle(0, y - h / 2, 0, h))
      Pane.requestFocusInWindow()
    }
    val panel = new JPanel(new BorderLayout())
    val map = scroll.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)
    map.allKeys().foreach(map.put(_, "none"))
    map.put(KeyStroke.getKeyStroke("ctrl pressed UP")       , "unitScrollUp"  )
    map.put(KeyStroke.getKeyStroke("ctrl pressed DOWN")     , "unitScrollDown")
    map.put(KeyStroke.getKeyStroke("ctrl pressed PAGE_UP")  , "scrollUp"      )
    map.put(KeyStroke.getKeyStroke("ctrl pressed PAGE_DOWN"), "scrollDown"    )
    panel.add(scroll, BorderLayout.CENTER)
    panel.add(stripe, BorderLayout.EAST)
    swing.Component.wrap(panel)
  }

  def actions: EditorActions = controller.actions

  def pane: swing.Component = swing.Component.wrap(Pane)

  def text: String = document.text

  def text_=(s: String): Unit =
    history.capture(document, terminal) {
      terminal.offset     = 0
      terminal.selection  = None
      terminal.highlights = Nil
      document.text       = s
    }

  private var _message: Option[String] = None

  def message: Option[String] = _message

  protected def message_=(m: Option[String]): Unit =
    if (_message != m) {
      _message = m
      notifyObservers()
    }

  def dispose(): Unit = {
    tooltipHandler.dispose()
    timer.stop()
  }

  def terminal: Terminal = MyTerminal

  document.onChange { _ =>
    val size = grid.toSize(document.linesCount, document.maximumIndent)
    if (Pane.getPreferredSize != size) {
      Pane.setPreferredSize(size)
      Pane.revalidate()
    }
  }

  terminal.onChange {
    case CaretMovement(_, to) =>
      scrollToOffsetVisible(to)
      updateMessage()
    case HighlightsChange(_, to) =>
      to.headOption.foreach(it => scrollToOffsetVisible(it.begin))
    case _ =>
  }

  holder.onChange { _ =>
    updateMessage()
  }

  private def updateMessage(): Unit =
    message = errorAt(terminal.offset).map(_.message)

  private var popupVisible = false

  private def toPoint(offset: Int): Point = grid.toPoint(document.toLocation(offset))

  private val tooltipHandler = new TooltipHandler(Pane,
    point => document.toOffset(grid.toLocation(point)).flatMap(errorAt))

  private val timer = new Timer(500, new ActionListener() {
    def actionPerformed(e: ActionEvent): Unit =
      if (shouldDisplayCaret) {
        canvas.caretVisible = !canvas.caretVisible
      }
  })

  private def shouldDisplayCaret = Pane.isFocusOwner || popupVisible

  private val painters = PainterFactory.createPainters(document, terminal, data,
    canvas, grid, lexer, matcher, holder, styling, font, controller)

  painters.foreach(painter => painter.onChange(handlePaintingRequest(painter, _)))

  private val handlePaintingRequest = (painter: Painter, rectangle: Rectangle) => {
    if (canvas.visible) {
      val visibleRectangle = rectangle.intersection(canvas.visibleRectangle)

      if (!visibleRectangle.isEmpty) {
        if (painter.immediate) {
          Pane.paintImmediately(painters.filter(_.immediate))
        } else {
          Pane.repaint(visibleRectangle)
        }
      }
    }
  }

  timer.start()

  // handle external changes
  document.onChange { _ =>
    terminal.offset     = terminal.offset.min(document.length)
    val selection       = terminal.selection.map(it => Interval(it.begin.min(document.length), it.end.min(document.length)))
    terminal.selection  = selection.filterNot(_.empty)
    terminal.highlights = Nil
  }

  private def scrollToOffsetVisible(offset: Int): Unit = {
    val w = grid.cellWidth
    val h = grid.cellHeight
    val p = toPoint(offset)

    val spot = {
      val panelBounds = Pane.getBounds(null)
      panelBounds.setLocation(0, 0)
      panelBounds.intersection(new Rectangle(p.x - w * 2, p.y - h, w * 4, h * 3))
    }

    if (!scroll.getViewport.getViewRect.contains(spot)) {
      Pane.scrollRectToVisible(spot)
    }
  }

  private def updateCaret(): Unit = {
    canvas.caretVisible = shouldDisplayCaret
    timer.restart()
  }

  private def errorAt(offset: Int): Option[Error] = {
    val errors = holder.errors.filter(_.interval.withEndShift(1).includes(offset))
    errors.sortBy(!_.fatal).headOption
  }

  Pane.addKeyListener(new KeyAdapter() {
    override def keyPressed(e: KeyEvent): Unit = {
      controller.processKeyPressed(e)
      updateCaret()
    }

    override def keyTyped(e: KeyEvent): Unit = {
      controller.processKeyTyped(e)
      updateCaret()
    }
  })

  Pane.addMouseListener(new MouseAdapter() {
    override def mousePressed(e: MouseEvent): Unit = {
      controller.processMousePressed(e)
      updateCaret()
      Pane.requestFocusInWindow()
    }
  })

  Pane.addMouseMotionListener(new MouseMotionAdapter() {
    override def mouseDragged(e: MouseEvent): Unit = {
      controller.processMouseDragged(e)
      updateCaret()
    }

    override def mouseMoved(e: MouseEvent): Unit =
      controller.processMouseMoved(e)
  })

  Pane.addFocusListener(new FocusListener {
    def focusGained (e: FocusEvent): Unit = updateCaret()
    def focusLost   (e: FocusEvent): Unit = updateCaret()
  })

  private object MyTerminal extends AbstractTerminal {
    def choose[A](variants: ISeq[A], query: String)(callback: A => Unit): Unit = {
      val point = toPoint(offset)
      val shifted = new Point(point.x - grid.cellWidth * query.length - 3, point.y + 20)
      val (popup, list) = ChooserFactory.createPopup(Pane, shifted, regularFont, variants, listRenderer) { it =>
        Pane.requestFocusInWindow() // to draw cursor immediately
        popupVisible = false
        it.foreach(callback)
      }
      popup.show()
      list.requestFocusInWindow()
      popupVisible = true
    }

    def edit(text: String, title: String)(callback: Option[String] => Unit): Unit = {
      val dialog = DialogFactory.create(Pane, text, title) { result =>
        Pane.requestFocusInWindow() // to draw cursor immediately
        callback(result)
      }
      dialog.pack()
      dialog.setLocationRelativeTo(scroll)
      dialog.setVisible(true)
    }
  }

  private object Pane extends JComponent with Scrollable {
    setOpaque(true)
    setBorder(new EmptyBorder(/* top */ 5, /* left */ 5, /* bottom */ 5, /* right */ 5))
    setFocusable(true)
    setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR))
    setFocusTraversalKeysEnabled(false)

    def getPreferredScrollableViewportSize: Dimension = getPreferredSize

    def getScrollableUnitIncrement(visibleRect: Rectangle, orientation: Int, direction: Int): Int = orientation match {
      case SwingConstants.VERTICAL    => grid.cellHeight
      case SwingConstants.HORIZONTAL  => grid.cellWidth
    }

    def getScrollableBlockIncrement(visibleRect: Rectangle, orientation: Int, direction: Int): Int = orientation match {
      case SwingConstants.VERTICAL    => visibleRect.height
      case SwingConstants.HORIZONTAL  => visibleRect.width
    }

    def getScrollableTracksViewportWidth: Boolean = getParent match {
      case vp: JViewport if vp.getWidth > getPreferredSize.width => true
      case _ => false
    }

    def getScrollableTracksViewportHeight: Boolean = getParent match {
      case vp: JViewport if vp.getHeight > getPreferredSize.height => true
      case _ => false
    }

    override def paintComponent(g: Graphics): Unit =
      paintOn(g, painters.filterNot(_.immediate))

    def paintImmediately(painters: ISeq[Painter]): Unit =
      Option(getGraphics).foreach { graphics =>
        paintOn(graphics, painters)
        Toolkit.getDefaultToolkit.sync()
      }

    private def paintOn(g: Graphics, painters: ISeq[Painter]): Unit = {
      val g2d = g.asInstanceOf[Graphics2D]

      renderingHints.foreach { it =>
        g2d.addRenderingHints(it.asInstanceOf[java.util.Map[_, _]])
      }

      val clipBounds = g.getClipBounds
      painters.foreach { p =>
        p.paint(g, clipBounds)
      }
    }
  }
}