package ba.sake.swebserver

import java.nio.file.Paths
import io.undertow.{Handlers, Undertow}
import io.undertow.server.handlers.resource.{PathResourceManager, ResourceHandler}
import mainargs.{ParserForMethods, arg, main}

object Main {

  @main
  def run(
      @arg(short = 'd', doc = "Path to folder to serve, default is current folder")
      directory: String = ".",
      @arg(short = 'h', doc = "Server host, default is localhost")
      host: String = "localhost",
      @arg(short = 'p', doc = "Server port, default is 5555")
      port: Int = 5555,
  ): Unit = {
    val baseFolder = Paths.get(directory).toAbsolutePath
    val resourceManager = new PathResourceManager(baseFolder)
    val fileHandler = SwebserverFileHandler(os.Path(baseFolder), host, port, ResourceHandler(resourceManager))
    val changes = new java.util.concurrent.atomic.AtomicBoolean(false)
    val websocketHandler = Handlers.websocket(SwebserverWebSocketConnectionCallback(changes))
    val server = Undertow
      .builder()
      .addHttpListener(port, host)
      .setHandler(
        Handlers
          .path()
          .addPrefixPath("/ws", websocketHandler)
          .addPrefixPath("/", fileHandler)
      )
      .build()
    server.start()
    os.watch.watch(
      Seq(os.Path(baseFolder) / "_site"),
      _ => changes.set(true)
    )
    println(s"HTTP server started on http://${host}:${port} ; Watching files in: ${baseFolder.toString}")
  }

  def main(args: Array[String]): Unit = ParserForMethods(this).runOrExit(args)
}
