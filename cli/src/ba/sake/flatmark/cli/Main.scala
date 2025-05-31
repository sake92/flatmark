package ba.sake.flatmark.cli

import mainargs.{Flag, ParserForMethods, arg, main}

import java.nio.file.Paths
import java.util.logging.Level

object Main {

  @main
  def run(
      @arg(short = 'i', doc = "Path to input directory, default is current directory")
      input: String = ".",
      @arg(doc = "Server port, default is 8181")
      port: Int = 8181,
      @arg(doc = "Log level, default is INFO, can be one of: FINE, INFO, WARNING, SEVERE") // TODO nicer enum
      logLevel: String = "INFO",
      @arg(doc = "Skip cache for generated files, default is false")
      noCache: Flag
  ): Unit = {
    val siteRootFolder = os.Path(Paths.get(input).toAbsolutePath)
    val logLevelValue = Level.parse(logLevel)
    FlatmarkCli(siteRootFolder, port,logLevelValue, !noCache.value).run()
  }

  def main(args: Array[String]): Unit = ParserForMethods(this).runOrExit(args)
}
