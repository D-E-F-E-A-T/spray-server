package spray.server

import akka.actor.{ Props, ActorSystem }
import spray.servlet._

/**
 * 项目启动时加载，在application.conf中配置
 */
class Boot extends WebBoot {

  /**
   * 创建ActorSystem
   */
  val system = ActorSystem("web")

  /**
   * 匹配Actor
   */
  val serviceActor = system.actorOf(Props[Router])

  system.registerOnTermination {
    // 系统销毁时触发
    system.log.info("Application shut down")
  }
}