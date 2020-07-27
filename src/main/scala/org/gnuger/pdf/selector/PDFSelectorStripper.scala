package org.gnuger.pdf.selector

import java.io.StringWriter
import java.util

import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.text.{PDFTextStripper, TextPosition}

import scala.collection.mutable
import scala.jdk.CollectionConverters._
import scala.util.matching.Regex

case class PDFSelectorStripper(startPlaceholder: Regex, endPlaceholder: Regex) extends PDFTextStripper {

  import PDFSelectorStripper._

  var rects = mutable.ListBuffer.empty[Rect]
  var state = 0
  var positions = mutable.ListBuffer.empty[Position]

  override def writeString(text: String, textPositions: util.List[TextPosition]): Unit = {
    state match {
      case 0 => {
        // TODO: handle endPlaceHolder in the same line
        startPlaceholder.findFirstMatchIn(text).map(_.start) match {
          case Some(startIndex) => {
            val newText = text.substring(startIndex)
            val newTextPositions = textPositions.subList(startIndex, textPositions.size)
            writeLineRect(newTextPositions)
            super.writeString(newText, newTextPositions)
            state = 1
          }
          case _ =>
        }
      }
      case 1 => {
        endPlaceholder.findFirstMatchIn(text).map(_.end) match {
          case Some(endIndex) => {
            val newText = text.substring(0, endIndex)
            val newTextPositions = textPositions.subList(0, endIndex)
            writeLineRect(newTextPositions)
            super.writeString(newText, newTextPositions)
            state = 2
          }
          case _ => {
            writeLineRect(textPositions)
            super.writeString(text, textPositions)
          }
        }
      }
      case 2 =>
    }
  }

  def writeLineRect(textPositions: util.List[TextPosition]) = rects.addOne(textPositionsToRect(textPositions.asScala.toSeq))

  /**
   *
   * Bottom Left as (0,0)
   *
   * @param textPositions
   * @return
   */
  def textPositionsToRect(textPositions: Seq[TextPosition]) = {
    val firstLetter = textPositions.head
    val lastLetter = textPositions.last
    val x1 = firstLetter.getX
    val y1 = firstLetter.getEndY
    val x2 = lastLetter.getX + lastLetter.getWidth
    val y2 = lastLetter.getEndY + lastLetter.getHeight

    Rect(x1, y1, x2, y2, x2 - x1, y2 - y1)
  }

  def getTextBySelector(doc: PDDocument) = {
    positions = mutable.ListBuffer.empty[Position]
    val os = new StringWriter()
    writeText(doc, os)
    val text = os.toString.trim

    Highlight(text, positions.toSeq)
  }

  override def writePageStart(): Unit = {
    rects = mutable.ListBuffer.empty[Rect]
    super.writePageStart()
  }

  override def writePageEnd(): Unit = {
    val boundingRect = Rect.boundaryRect(rects.toSeq)
    val position = Position(boundingRect, getCurrentPageNo, rects.toSeq)
    positions.addOne(position)
    super.writePageEnd()
  }
}

object PDFSelectorStripper {

  case class Highlight(text: String, positions: Seq[Position])

  case class Position(boundingRect: Rect, pageNumber: Int, rects: Seq[Rect])

  case class Rect(x1: Float, y1: Float, x2: Float, y2: Float, width: Float, height: Float)

  object Rect {
    def boundaryRect(rects: Seq[Rect]): Rect = {
      val x1 = rects.map(_.x1).min
      val y1 = rects.map(_.y1).min
      val x2 = rects.map(_.x2).max
      val y2 = rects.map(_.y2).max
      Rect(x1, y1, x2, y2, x2 - x1, y2 - y1)
    }
  }

}


