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
      "/Users/anant.khaitan/Documents/avalara_tax_docs/updated_in_sales_and_use_taxes_accommodations_and_linens.pdf",
      "Application\\. – This section",
      "Expiration\\. –",
      "/Users/anant.khaitan/Desktop/output.pdf"
    )*/

    /*highlight(
      "/Users/anant.khaitan/Documents/avalara_tax_docs/CityOfHornLake.pdf",
      "occupancy tax is levied",
      "Taxpayer Access Point",
      "/Users/anant.khaitan/Desktop/output.pdf"
    )*/

    highlight(
      "/Users/anant.khaitan/Documents/avalara_tax_docs/vermont.pdf",
      "Selling Merchandise",
      "Additional Information",
      "/Users/anant.khaitan/Desktop/output.pdf"
    )
  }

}