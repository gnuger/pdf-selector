package org.gnuger.pdf

import java.awt.Color
import java.io.File

import org.apache.pdfbox.pdmodel.PDPageContentStream.AppendMode
import org.apache.pdfbox.pdmodel.graphics.state.PDExtendedGraphicsState
import org.apache.pdfbox.pdmodel.{PDDocument, PDPageContentStream}
import org.gnuger.pdf.selector.PDFSelectorStripper
import org.gnuger.pdf.selector.PDFSelectorStripper.Position

object Demo {
  def highlight(filename: String, startPlaceHolder: String, endPlaceHolder: String, outputFilename: String) = {



    val pdfDocument = PDDocument.load(new File(filename))
    val pdfTextStripper = PDFSelectorStripper(startPlaceHolder.r, endPlaceHolder.r)
    pdfTextStripper.setSortByPosition(true)
    val highlight = pdfTextStripper.getTextBySelector(pdfDocument)

    highlight.positions.foreach(drawRect(pdfDocument, _))

    println(highlight.text)

    pdfDocument.save(new File(outputFilename))
    pdfDocument.close()
  }

  def drawRect(pdfDocument: PDDocument, position: Position) = {
    val boundaryRect = position.boundingRect
    val page = pdfDocument.getPage(position.pageNumber - 1)
    val contentStream = new PDPageContentStream(pdfDocument, page, AppendMode.APPEND, false)
    val extendedGraphicsState = new PDExtendedGraphicsState()
    extendedGraphicsState.setNonStrokingAlphaConstant(0.2f)
    contentStream.setGraphicsStateParameters(extendedGraphicsState)
    contentStream.setNonStrokingColor(Color.YELLOW)
    contentStream.addRect(boundaryRect.x1, boundaryRect.y1, boundaryRect.width, boundaryRect.height)
    contentStream.fill()


    extendedGraphicsState.setNonStrokingAlphaConstant(0.4f)
    contentStream.setNonStrokingColor(Color.CYAN)
    position.rects.foreach(rect => contentStream.addRect(rect.x1, rect.y1, rect.width, rect.height))
    contentStream.fill()

    contentStream.close()
  }

  def main(args: Array[String]): Unit = {

    /*highlight(
      "/Users/anant.khaitan/Desktop/Demo/MI/72-18-28 Horn Lake Occupancy Extended Repeal Date.pdf",
      "For purposes of this levy",
      "such",
      "/Users/anant.khaitan/Desktop/output1.pdf"
    )

    highlight(
      "/Users/anant.khaitan/Desktop/Demo/MI/sales_notice720807natcheztax.pdf",
      "For purposes of this levy",
      "such",
      "/Users/anant.khaitan/Desktop/output2.pdf"
    )*/

    highlight(
      "/Users/anant.khaitan/Desktop/Demo/vermont.pdf",
      "If you sell merchandise",
      "performed by service employees",
      "/Users/anant.khaitan/Desktop/output_ve.pdf"
    )
  }

}