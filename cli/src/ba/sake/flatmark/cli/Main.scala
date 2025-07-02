package ba.sake.flatmark.cli

import mainargs.{Flag, ParserForMethods, TokensReader, arg, main}

import java.net.{URI, URL}
import java.nio.file.Paths
import java.util as ju
import java.util.logging.{Level, LogManager}

object Main {

  @main
  def run(
      @arg(positional = true)
      command: String = "build",
      @arg(short = 'v', doc = "Print version and exit")
      version: Flag,
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
      noCache: Flag,
      @arg(doc = "Update theme, default is false")
      updateTheme: Flag
  ): Unit = {
    val siteRootFolder = os.Path(Paths.get(input).toAbsolutePath)
    val logLevelValue = logLevel.julLevel
    // set logging properties
    LogManager.getLogManager.readConfiguration(getClass.getClassLoader.getResource("logging.properties").openStream())
    LogManager.getLogManager.getLogger("").setLevel(logLevelValue) // set root logger level
    val cli = FlatmarkCli(siteRootFolder, host, port, !noCache.value, updateTheme.value)
    command.toLowerCase match {
      case _ if version.value =>
        println(s"Flatmark CLI ${getVersion}")
      case "version" =>
        println(getVersion)
      case "build" =>
        cli.build()
      case "serve" =>
        cli.serve()
      case _ =>
        println(s"Unknown command: $command. Use 'build' or 'serve'.")
        System.exit(1)
    }
  }

  def main(args: Array[String]): Unit = ParserForMethods(this).runOrExit(args)

  private def getVersion: String = {
    val clazz = classOf[Main.type]
    val className = clazz.getSimpleName + ".class"
    val classPath = clazz.getResource(className).toString
    if classPath.startsWith("jar") then {
      val manifestPath = classPath.substring(0, classPath.lastIndexOf("!") + 1) + "/META-INF/MANIFEST.MF"
      val manifest = new ju.jar.Manifest(URI.create(manifestPath).toURL.openStream)
      val attr = manifest.getMainAttributes
      attr.getValue("Implementation-Version")
    } else {
      "0.0.0-SNAPSHOT"
    }
  }
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
