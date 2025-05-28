package ba.sake.flatmark.cli

import mainargs.{Flag, ParserForMethods, arg, main}

import java.nio.file.Paths

object Main {

  @main
  def run(
      @arg(short = 'i', doc = "Path to input directory")
      input: String = ".",
      @arg(doc = "Server port, default is 8181")
      port: Int = 8181
  ): Unit = {
    val siteRootFolder = os.Path(Paths.get(input).toAbsolutePath)
    val cli = FlatmarkCli()
    cli.run(siteRootFolder)
  }

  def main(args: Array[String]): Unit = ParserForMethods(this).runOrExit(args)
}
