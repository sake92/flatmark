package ba.sake.flatmark.swebserver

import java.nio.file.Paths
import mainargs.{ParserForMethods, arg, main}
import io.undertow.Undertow
import io.undertow.server.handlers.resource.{PathResourceManager, ResourceHandler}

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
    Undertow.builder
      .addHttpListener(port, "localhost")
      .setHandler(new SWebServerHandler(baseFolder, new ResourceHandler(resourceManager)))
      .build
      .start()
    println(s"HTTP server started on http://localhost:${port} ; Watching files in: ${baseFolder.toString}")
  }

  def main(args: Array[String]): Unit = ParserForMethods(this).runOrExit(args)
}
