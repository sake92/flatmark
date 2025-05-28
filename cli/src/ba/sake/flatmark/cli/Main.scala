package ba.sake.flatmark.cli

import mainargs.{Flag, ParserForMethods, arg, main}

import java.nio.file.Paths

object Main {

  @main
  def run(
      @arg(short = 'i', doc = "Path to input directory, default is current directory")
      input: String = ".",
      @arg(doc = "Server port, default is 8181")
      port: Int = 8181,
      @arg(doc = "Skip cache for generated files, default is false")
      noCache: Flag
  ): Unit = {
    val siteRootFolder = os.Path(Paths.get(input).toAbsolutePath)
    FlatmarkCli(siteRootFolder, port, !noCache.value).run()
  }

  def main(args: Array[String]): Unit = ParserForMethods(this).runOrExit(args)
}
