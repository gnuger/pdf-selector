package org.gnuger.pdf.service


import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives
import akka.stream.scaladsl.{Sink, StreamConverters}
import akka.stream.{ActorMaterializer, Materializer}
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.text.PDFTextStripper
import org.gnuger.pdf.selector.PDFSelectorStripper
import org.gnuger.pdf.selector.PDFSelectorStripper.{Highlight, Position, Rect}
import spray.json.DefaultJsonProtocol

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import scala.io.StdIn
import scala.language.postfixOps
import scala.util.{Failure, Success}

object WebServer extends PDFRoutes with SwaggerRoutes {
  implicit val system = ActorSystem("my-system")
  implicit val ec = system.dispatcher
  implicit val mat = ActorMaterializer()

  def main(args: Array[String]): Unit = {

    val route =
      pathPrefix("viewer") {
        getFromResourceDirectory("viewer") // uses implicit ContentTypeResolver
      }


    val bindingFuture = Http().bindAndHandle(fulltextRoute ~ highlightRoute ~ swaggerRoute ~ route, "localhost", 8080)
    println(s"Server online at http://localhost:8080/\nPress RETURN to stop...")
    StdIn.readLine() // let it run until user presses return
    bindingFuture
      .flatMap(_.unbind()) // trigger unbinding from the port
      .onComplete(_ => system.terminate()) // and shutdown when done
  }


}

trait JsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val rectFormat = jsonFormat6(Rect.apply)
  implicit val positionFormat = jsonFormat3(Position.apply)
  implicit val highlightFormat = jsonFormat2(Highlight.apply)
}

trait PDFRoutes extends Directives with JsonSupport {
  implicit val ec: ExecutionContext
  implicit val mat: Materializer

  val fulltextRoute = (path("fulltext") & extractLog) { log =>
    entity(as[Multipart.FormData]) { formData =>
      val extractOperation = formDataToPDDocument(formData, "file").map(document => {
        val pdfTextStripper = new PDFTextStripper()
        pdfTextStripper.setSortByPosition(true)
        pdfTextStripper.getText(document)
      })

      onComplete(extractOperation) {
        case Success(text) => complete(text)
        case Failure(exception) => complete(s"Failed to parse pdf: $exception")
      }
    }
  }

  val highlightRoute = (path("select") & extractLog) { log =>
    parameters('start, 'end) { (startPlaceHolder, endPlaceHolder) =>
      entity(as[Multipart.FormData]) { formData =>
        val extractOperation = formDataToPDDocument(formData, "file").map(document => {
          val pdfSelectorStripper = PDFSelectorStripper(startPlaceHolder.r, endPlaceHolder.r)
          log.info(s"Extracting section from PDF between [$startPlaceHolder] and [$endPlaceHolder]")
          pdfSelectorStripper.setSortByPosition(true)
          pdfSelectorStripper.getTextBySelector(document)
        })

        onComplete(extractOperation) {
          case Success(highlight) => complete(highlight)
          case Failure(exception) => complete(s"Failed to parse pdf: $exception")
        }
      }
    }
  }

  def formDataToPDDocument(formData: Multipart.FormData, fieldName: String)(implicit mat: Materializer) = formData
    .parts
    .filter(_.name == fieldName)
    .map(_.entity.dataBytes.runWith(StreamConverters.asInputStream(1 minute)))
    .map(PDDocument.load(_))
    .runWith(Sink.head)
}

trait SwaggerRoutes extends Directives {
  val swaggerRoute = pathPrefix("swagger") {
    pathEnd {
      extractUri { uri =>
        redirect(uri + "/", StatusCodes.TemporaryRedirect)
      }
    } ~ pathSingleSlash {
      getFromResource("swagger/index.html")
    } ~ getFromResourceDirectory("swagger")
  }
}
