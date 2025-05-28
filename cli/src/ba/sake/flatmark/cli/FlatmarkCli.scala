package ba.sake.flatmark.cli

import java.util.logging.Logger
import java.util.logging.LogManager
import ba.sake.flatmark.{ChromeDriverHolder, FlatmarkGenerator}
import ba.sake.sharaf.undertow.UndertowSharafServer

class FlatmarkCli(port: Int) {
  private val logger = Logger.getLogger(getClass.getName)

  def run(siteRootFolder: os.Path): Unit = {
    // set logging properties
    LogManager.getLogManager.readConfiguration(getClass.getClassLoader.getResource("logging.properties").openStream())

    logger.info("Flatmark started")

    val startAtMillis = System.currentTimeMillis()
    val chromeDriverHolder = ChromeDriverHolder()
    val flatmarkServer = startFlatmarkServer()
    val generator = FlatmarkGenerator(port, chromeDriverHolder)
    try generator.generate(siteRootFolder)
    finally
      chromeDriverHolder.close()
      flatmarkServer.stop()
    val finishAtMillis = System.currentTimeMillis()
    val totalSeconds = (finishAtMillis - startAtMillis).toDouble / 1000
    logger.info(s"Flatmark finished in ${totalSeconds} s")
  }

  private def startFlatmarkServer(): UndertowSharafServer =
    logger.fine("Flatmark server starting...")
    val flatmarkServerRoutes = ba.sake.flatmark.ssr.routes
    val server = UndertowSharafServer("localhost", 8181, flatmarkServerRoutes)
    server.start()
    logger.fine("Flatmark server started")
    server
}
