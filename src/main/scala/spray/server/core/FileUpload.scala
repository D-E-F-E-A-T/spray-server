package spray.server.core

import akka.actor._
import scala.concurrent.duration._
import java.io.{ InputStream, FileInputStream, FileOutputStream, File }
import org.jvnet.mimepull.{ MIMEPart, MIMEMessage }
import spray.http._
import MediaTypes._
import HttpHeaders._
import parser._
import HttpHeaders._
import spray.io._
import scala.annotation._
import java.util._
import spray.server.page._

class FileUpload(client: ActorRef, start: ChunkedRequestStart, filePath: String, fileName: String) extends Actor with ActorLogging {
  import start.request._
  import collection.JavaConverters._

  client ! CommandWrapper(SetRequestTimeout(Duration.Inf)) // cancel timeout

  val tmpFile = new File(filePath) //存到哪里，这个路径必须要存在
  tmpFile.deleteOnExit

  val output = new FileOutputStream(tmpFile)
  val Some(HttpHeaders.`Content-Type`(ContentType(multipart: MultipartMediaType, _))) = header[HttpHeaders.`Content-Type`]
  val file = multipart.parameters(fileName)

  log.info(s"Got start of chunked request $method $uri with multipart boundary '$file' writing to $tmpFile")
  var bytesWritten = 0L

  def receive = {
    case c: MessageChunk =>
      log.debug(s"Got ${c.data.length} bytes of chunked request $method $uri")

      output.write(c.data.toByteArray)
      bytesWritten += c.data.length

    case e: ChunkedMessageEnd =>
      log.info(s"Got end of chunked request $method $uri")
      output.close

      val message = new MIMEMessage(new FileInputStream(tmpFile), file)
      val parts = message.getAttachments.asScala.toSeq

      client ! &(fileUpload(bytesWritten, parts))
      client ! CommandWrapper(SetRequestTimeout(2.seconds)) // reset timeout to original value
      tmpFile.delete
      context.stop(self)
  }
}