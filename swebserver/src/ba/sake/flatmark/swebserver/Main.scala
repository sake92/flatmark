package ba.sake.flatmark.swebserver

import java.nio.file.Paths
import mainargs.{ParserForMethods, arg, main}
import io.undertow.Undertow

object Main {

  @main
  def run(
      @arg(short = 'd', doc = "Path to directory to server, default is current directory")
      directory: String = ".",
      @arg(doc = "Server port, default is 5555")
      port: Int = 5555
  ): Unit = {
    val baseFolder = Paths.get(directory).toAbsolutePath
    Undertow.builder.addHttpListener(port, "localhost").setHandler(new SWebServerHandler(baseFolder)).build.start()
    System.out.println(s"Server started on http://localhost:${port}")
  }

  def main(args: Array[String]): Unit = ParserForMethods(this).runOrExit(args)
}
