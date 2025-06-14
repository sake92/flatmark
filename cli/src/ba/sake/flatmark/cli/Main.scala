package ba.sake.flatmark.cli

import mainargs.{Flag, ParserForMethods, TokensReader, arg, main}

import java.nio.file.Paths
import java.util.logging.Level

object Main {

  @main
  def run(
      @arg(positional = true)
      command: String = "build",
      @arg(short = 'i', doc = "Path to input folder, default is current folder")
      input: String = ".",
      @arg(short = 'h', doc = "Server host, default is localhost")
      host: String = "localhost",
      @arg(short = 'p', doc = "Server port, default is 5555")
      port: Int = 5555,
      @arg(
        short = 'l',
        doc = "Log level, default is info. One of: debug, info, error"
      )
      logLevel: LogLevel = LogLevel.INFO,
      @arg(doc = "Skip file cache, default is false")
      noCache: Flag
  ): Unit = {
    val siteRootFolder = os.Path(Paths.get(input).toAbsolutePath)
    val logLevelValue = logLevel.julLevel
    val cli = FlatmarkCli(siteRootFolder, host, port, logLevelValue, !noCache.value)
    command.toLowerCase match {
      case "build" => cli.build()
      case "serve" => cli.serve()
      case _ =>
        println(s"Unknown command: $command. Use 'build' or 'serve'.")
        System.exit(1)
    }
  }

  def main(args: Array[String]): Unit = ParserForMethods(this).runOrExit(args)
}

enum LogLevel(val julLevel: Level) {
  case DEBUG extends LogLevel(Level.FINE)
  case INFO extends LogLevel(Level.INFO)
  case ERROR extends LogLevel(Level.SEVERE)
}

given TokensReader.Simple[LogLevel] with {
  def shortName = "logLevel"
  def read(strs: Seq[String]) = Right(LogLevel.valueOf(strs.head.toUpperCase))
}
