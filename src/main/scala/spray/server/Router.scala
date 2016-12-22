package spray.server

import scala.collection.mutable._
import scala.concurrent.duration._
import akka.io.Tcp
import akka.actor._
import spray.http._
import MediaTypes._
import HttpMethods._

class Router extends Actor with ActorLogging {

  // 参考https://github.com/spray/spray
  def receive = {

    case request @ HttpRequest(GET, Uri.Path("/"), _, _, _) =>
      println(request.uri) // 暂未找到解析uri的方式
      sender ! index

    case HttpRequest(GET, Uri.Path("/ping"), _, _, _) =>
      sender ! HttpResponse(entity = "PONG!")

    case HttpRequest(GET, Uri.Path("/stream"), _, _, _) =>
      val client = sender // 长连接
      context.actorOf(Props(new Streamer(client, 20)))

    case HttpRequest(GET, Uri.Path("/crash"), _, _, _) =>
      sender ! HttpResponse(entity = "About to throw an exception in the request handling actor, " +
        "which triggers an actor restart")
      sys.error("BOOM!") // 自定义异常

    case HttpRequest(GET, Uri.Path("/timeout"), _, _, _) =>
      log.info("Dropping request, triggering a timeout")

    case HttpRequest(GET, Uri.Path("/changetimeout"), _, _, _) =>
      sender ! SetRequestTimeout(60.seconds)
      log.info("Changing timeout initially set by 'spray.servlet.request-timeout' property and triggering timeout")

    case HttpRequest(GET, Uri.Path("/timeout/timeout"), _, _, _) =>
      log.info("Dropping request, triggering a timeout")

    case _: HttpRequest => sender ! HttpResponse(404, "404 Unknown resource!")

    case Timedout(HttpRequest(_, Uri.Path("/timeout/timeout"), _, _, _)) =>
      log.info("Dropping Timeout message")

    case Timedout(request: HttpRequest) =>
      sender ! HttpResponse(500, "The " + request.method + " request to '" + request.uri + "' has timed out...")
  }

  ////////////// helpers //////////////

  lazy val index = HttpResponse(
    entity = HttpEntity(`text/html`, toAscii("""
      <!doctype html>
      <html>
        <head>
          <meta charset="utf-8" />
          <title>中文标题</title>
        </head>
        <body>
          <h1>Spray框架Demo</h1>
          <p>Defined resources:</p>
          <ul>
            <li><a href="ping">ping</a></li>
            <li><a href="stream">stream</a></li>
            <li><a href="crash">crash</a></li>
            <li><a href="timeout">timeout</a></li>
            <li><a href="timeout/timeout">timeout/timeout</a></li>
          </ul>
        </body>
      </html>""")))

  def toAscii(text: String) = {
    val sb = new StringBuffer
    for (t <- text.toCharArray)
      if (t.toInt > 19967 && t.toInt < 40870)
        sb.append("&#").append(t.toInt).append(";")
      else
        sb.append(t)

    sb.toString
  }
}