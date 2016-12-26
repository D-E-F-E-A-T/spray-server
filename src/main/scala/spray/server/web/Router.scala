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
import java.util._

class Router extends Actor with ActorLogging {

  def receive = {

    case HttpRequest(GET, Uri.Path(""), _, _, _) =>
      sender ! *("/") //重定向，Spray取不到项目名，要么写死，要么用相对路径

    case request @ HttpRequest(GET, Uri.Path("/"), _, _, _) =>
      println(request.uri.query) // 在Spray中""和"/"是两个完全不同的路径
      sender ! &(index())

    case HttpRequest(GET, Uri.Path("/ping"), _, _, _) =>
      sender ! %("PONG!")

    case request @ HttpRequest(POST, Uri.Path("/file-upload"), headers, entity: HttpEntity.NonEmpty, protocol) =>
      val filePath = "/tmp/" + UUID.randomUUID //文件存储到哪里，叫什么名
      val fileName = "file" //取哪个name下的文件

      val parts = request.asPartStream() //文件上传
      val actor = new FileUpload(sender, parts.head.asInstanceOf[ChunkedRequestStart], filePath, fileName)
      val handler = context.actorOf(Props(actor))
      parts.tail.foreach(handler !)

    case HttpRequest(GET, Uri.Path("/stream"), _, _, _) =>
      val client = sender // 长连接
      context.actorOf(Props(new Streamer(client, 20)))

    case HttpRequest(GET, Uri.Path("/crash"), _, _, _) =>
      sender ! %("重启Actor")
      sys.error("BOOM!") // 自定义异常

    case HttpRequest(GET, Uri.Path("/timeout"), _, _, _) =>
      log.info("没有send返回值，等待超时")

    case HttpRequest(GET, Uri.Path("/timeout/timeout"), _, _, _) =>
      log.info("没有send返回值，等待超时，超时异常特殊捕获")

    case HttpRequest(GET, Uri.Path("/changetimeout"), _, _, _) =>
      sender ! SetRequestTimeout(60.seconds)
      log.info("将超时时间修改为60秒")

    case _: HttpRequest => sender ! HttpResponse(404, "404 资源未找到")

    case Timedout(HttpRequest(_, Uri.Path("/timeout/timeout"), _, _, _)) =>
      sender ! %("捕获/timeout/timeout超时异常")

    case Timedout(request: HttpRequest) =>
      sender ! HttpResponse(500, "500 服务器响应超时")
  }
}