package spray.server

import spray.http._
import MediaTypes._
import scala.io._

/**
 * @author vermisse
 */
object % {

  def apply(text: String = null, html: String = null, page: String = null) = {
    if (text != null) {
      HttpResponse(entity = text)
    } else if (html != null) {
      HttpResponse(entity = HttpEntity(`text/html`, ascii(html)))
    } else if (page != null) {
      val path = this.getClass.getResource("/").getPath.replace("WEB-INF/classes/", "WEB-INF/view/")
      HttpResponse(entity = HttpEntity(`text/html`, ascii(file(path + page))))
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

  /**
   * 读取文件内容
   */
  private def file(path: String) = {
    val file = Source.fromFile(path)
    val result = new StringBuffer
    try {
      file.getLines.foreach { result.append(_) }
      result.toString
    } finally {
      file.close //自定义租赁模式，既使用后自动关闭，调用的时候无需考虑
    }
  }
}