package spray.server.page

import scala.concurrent.duration._
import java.io.{InputStream, FileInputStream, FileOutputStream, File}
import org.jvnet.mimepull.{MIMEPart, MIMEMessage}
import spray.http._
import MediaTypes._
import HttpHeaders._
import parser._
import HttpHeaders._
import spray.io._
import scala.annotation._
import java.util._
import collection.JavaConverters._

object fileUpload {
  def apply(bytesWritten: Long, parts: Seq[MIMEPart]) = {
    <html>
      <head>
        <meta chatset="UTF-8"/>
      </head>
      <body>
        <p>Got { bytesWritten } bytes</p>
        <h3>Parts</h3>
        {
          parts.map { part =>
            val name = fileNameForPart(part).getOrElse("<unknown>")
            <div>{ name }: { part.getContentType } of size { sizeOf(part.readOnce) }</div>
          }
        }
      </body>
    </html>
  }

  def fileNameForPart(part: MIMEPart) = {
    for {
      dispHeader <- part.getHeader("Content-Disposition").asScala.toSeq.lift(0)
      Right(disp: `Content-Disposition`) = HttpParser.parseHeader(RawHeader("Content-Disposition", dispHeader))
      name <- disp.parameters.get("filename")
    } yield name
  }

  def sizeOf(is: InputStream) = {
    val buffer = new Array[Byte](65000)

    @tailrec def inner(cur: Long): Long = {
      val read = is.read(buffer)
      if (read > 0) inner(cur + read)
      else cur
    }

    inner(0)
  }
}