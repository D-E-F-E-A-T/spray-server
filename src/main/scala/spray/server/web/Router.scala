package spray.server.web

import scala.collection.mutable._
import scala.concurrent.duration._
import akka.actor._
import spray.http._
import spray.http.MediaTypes._
import spray.http.HttpMethods._
import spray.http.HttpEntity._
import spray.http.StatusCode._
import spray.server.core._
import spray.server.page._

class Router extends Actor with ActorLogging {

  def receive = {

    case request @ HttpRequest(GET, Uri.Path(""), _, _, _) =>
      println(request.uri) // 暂未找到解析uri的方式
      sender ! %(page = index())

    case request @ HttpRequest(GET, Uri.Path("/"), _, _, _) =>
      println(request.uri) // 在Spray中""和"/"是两个完全不同的路径
      sender ! %(page = index())

    case HttpRequest(GET, Uri.Path("/ping"), _, _, _) =>
      sender ! %("PONG!")

    case request @ HttpRequest(POST, Uri.Path("/file-upload"), headers, entity: HttpEntity.NonEmpty, protocol) =>
      val parts = request.asPartStream() //文件上传
      val handler = context.actorOf(Props(new FileUpload(sender, parts.head.asInstanceOf[ChunkedRequestStart])))
      parts.tail.foreach(handler !)

    case HttpRequest(GET, Uri.Path("/stream"), _, _, _) =>
      val client = sender // 长连接
      context.actorOf(Props(new Streamer(client, 20)))

    case HttpRequest(GET, Uri.Path("/crash"), _, _, _) =>
      sender ! %(text = "About to throw an exception in the request handling actor, which triggers an actor restart")
      sys.error("BOOM!") // 自定义异常

    case HttpRequest(GET, Uri.Path("/timeout"), _, _, _) =>
      log.info("Dropping request, triggering a timeout")

    case HttpRequest(GET, Uri.Path("/changetimeout"), _, _, _) =>
      sender ! SetRequestTimeout(60.seconds)
      log.info("Changing timeout initially set by 'spray.servlet.request-timeout' property and triggering timeout")

    case HttpRequest(GET, Uri.Path("/timeout/timeout"), _, _, _) =>
      log.info("Dropping request, triggering a timeout")

    case _: HttpRequest => sender ! HttpResponse(404, "404 资源未找到")

    case Timedout(HttpRequest(_, Uri.Path("/timeout/timeout"), _, _, _)) =>
      log.info("Dropping Timeout message")

    case Timedout(request: HttpRequest) =>
      sender ! HttpResponse(500, "500 服务器响应超时")
  }
}