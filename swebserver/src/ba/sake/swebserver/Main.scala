package ba.sake.swebserver

import java.nio.file.Paths
import io.undertow.{Handlers, Undertow}
import io.undertow.server.handlers.resource.{PathResourceManager, ResourceHandler}
import mainargs.{ParserForMethods, arg, main}

object Main {

  @main
  def run(
      @arg(short = 'd', doc = "Path to directory to server, default is current directory")
      directory: String = ".",
      @arg(doc = "Server port, default is 5555")
      port: Int = 5555
  ): Unit = {
    val baseFolder = Paths.get(directory).toAbsolutePath
    val resourceManager = new PathResourceManager(baseFolder)
    val fileHandler = SwebserverFileHandler(os.Path(baseFolder), "localhost", port, ResourceHandler(resourceManager))
    val changes = new java.util.concurrent.atomic.AtomicBoolean(false)
    val websocketHandler = Handlers.websocket(SwebserverWebSocketConnectionCallback(changes))
    val server = Undertow
      .builder()
      .addHttpListener(port, "localhost")
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
    println(s"HTTP server started on http://localhost:${port} ; Watching files in: ${baseFolder.toString}")
  }

  def main(args: Array[String]): Unit = ParserForMethods(this).runOrExit(args)
}
