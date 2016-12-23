package spray.server.core

import spray.http._
import spray.http.MediaTypes._
import scala.io._
import spray.http.ContentType._
import spray.http.HttpEntity._
import scala.xml._

/**
 * @author vermisse
 */
object % {

  def apply(text: String = null, html: String = null, page: Elem = null) = {
    if (text != null) {
      HttpResponse(entity = text)
    } else if (html != null) {
      HttpResponse(entity = HttpEntity(`text/html`, ascii(html)))
    } else if (page != null) {
      HttpResponse(entity = HttpEntity(`text/html`, ascii("<!doctype html>\n" + page)))
    } else { null }
  }

  /**
   * 中文转码
   */
  def ascii(text: String) = {
    val sb = new StringBuffer
    for (t <- text.toCharArray)
      if (t.toInt > 19967 && t.toInt < 40870)
        sb.append("&#").append(t.toInt).append(";")
      else
        sb.append(t)

    sb.toString
  }
}