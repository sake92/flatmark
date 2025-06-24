package ba.sake.swebserver

import java.util.concurrent.atomic.{AtomicBoolean, AtomicReference}
import scala.jdk.CollectionConverters.*
import org.slf4j.LoggerFactory
import io.undertow.websockets.WebSocketConnectionCallback
import io.undertow.websockets.core.{AbstractReceiveListener, StreamSourceFrameChannel, WebSocketChannel, WebSockets}
import io.undertow.websockets.spi.WebSocketHttpExchange

import java.time.Instant

class SwebserverWebSocketConnectionCallback(lastChangeAt: AtomicReference[Instant])
    extends WebSocketConnectionCallback {
  private val logger = LoggerFactory.getLogger(getClass.getName)

  private val clients = new java.util.concurrent.ConcurrentHashMap[WebSocketChannel, Boolean]()

  private var lastUpdateAt: Instant = Instant.MIN

  private val t = new Thread(() => {
    while true do {
      if lastChangeAt.get().isAfter(lastUpdateAt) then {
        lastUpdateAt = Instant.now()
        clients.keys.asScala.foreach { channel =>
          if channel.isOpen then WebSockets.sendText("reload", channel, null)
        }
      }
      Thread.sleep(500) // Check every 100ms at max
    }
  })
  t.setDaemon(true)
  t.start()

  override def onConnect(exchange: WebSocketHttpExchange, channel: WebSocketChannel): Unit = {
    clients.put(channel, true)
    logger.debug(s"WebSocket client connected: ${channel.getPeerAddress}")

    channel.getReceiveSetter.set(new AbstractReceiveListener {
      override def onClose(webSocketChannel: WebSocketChannel, channel: StreamSourceFrameChannel): Unit = {
        clients.remove(webSocketChannel)
        logger.debug(s"WebSocket client disconnected: ${webSocketChannel.getPeerAddress}")
      }
    })
    channel.resumeReceives()
  }
}
