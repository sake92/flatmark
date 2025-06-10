package ba.sake.flatmark.cli

import mainargs.{Flag, ParserForMethods, arg, main}

import java.nio.file.Paths
import java.util.logging.Level

object Main {

  @main
  def run(
      @arg(positional = true)
      command: String = "build",
      @arg(short = 'i', doc = "Path to input directory, default is current directory")
      input: String = ".",
      @arg(doc = "Server port, default is 5555")
      port: Int = 5555,
      @arg(doc = "Log level, default is INFO, can be one of: FINE, INFO, WARNING, SEVERE") // TODO nicer enum
      logLevel: String = "INFO",
      @arg(doc = "Skip file cache, default is false")
      noCache: Flag
  ): Unit = {
    val siteRootFolder = os.Path(Paths.get(input).toAbsolutePath)
    val logLevelValue = Level.parse(logLevel)
    command.toLowerCase match {
      case "build" =>
        FlatmarkCli(siteRootFolder, port, logLevelValue, !noCache.value).build()
      case "serve" =>
        FlatmarkCli(siteRootFolder, port, logLevelValue, !noCache.value).serve()
      case _ =>
        println(s"Unknown command: $command. Use 'build' or 'serve'.")
        System.exit(1)
    }
    
  }

  def main(args: Array[String]): Unit = ParserForMethods(this).runOrExit(args)
}
