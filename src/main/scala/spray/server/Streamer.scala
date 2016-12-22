package spray.server

import scala.concurrent.duration._
import akka.actor._
import spray.http._
import akka.io.Tcp

/**
 * 长连接
 */
class Streamer(client: ActorRef, count: Int) extends Actor with ActorLogging {
  log.debug("Starting streaming response ...")

  // we use the successful sending of a chunk as trigger for scheduling the next chunk
  client ! ChunkedResponseStart(HttpResponse(entity = " " * 2048)).withAck(Ok(count))

  def receive = {
    case Ok(0) =>
      log.info("Finalizing response stream ...")
      client ! MessageChunk("\nStopped...")
      client ! ChunkedMessageEnd
      context.stop(self)

    case Ok(remaining) =>
      log.info("Sending response chunk ...")
      import context.dispatcher
      context.system.scheduler.scheduleOnce(100.millis) {
        client ! MessageChunk(DateTime.now.toIsoDateTimeString + ", ").withAck(Ok(remaining - 1))
      }

    case ev: Tcp.ConnectionClosed =>
      log.info("Canceling response stream due to {} ...", ev)
      context.stop(self)
  }
}

case class Ok(remaining: Int)