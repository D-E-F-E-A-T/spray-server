package spray.server.core

import spray.http._
import spray.http.MediaTypes._
import scala.io._
import spray.http.ContentType._
import spray.http.HttpEntity._
import scala.xml._

/**
 * 输出文本
 * @author vermisse
 */
object % {
  def apply(text: String) = HttpResponse(entity = text)
}

/**
 * 输出HTML
 */
object & {
  def apply(html: String) = HttpResponse(entity = HttpEntity(`text/html`, ascii(html)))
  def apply(page: Elem) = HttpResponse(entity = HttpEntity(`text/html`, ascii("<!doctype html>\n" + page)))
}

/**
 * 重定向
 */
object * {
  def apply(uri: String) = HttpResponse(302, headers = List(HttpHeaders.Location(Uri(uri))))
}

/**
 * 汉字转码
 */
object ascii {
  def apply(text: String) = {
    val sb = new StringBuffer
    for (t <- text.toCharArray)
      if (t.toInt > 19967 && t.toInt < 40870)
        sb.append("&#").append(t.toInt).append(";")
      else
        sb.append(t)

    sb.toString
  }
}