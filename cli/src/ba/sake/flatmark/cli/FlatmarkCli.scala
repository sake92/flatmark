package ba.sake.flatmark.cli

import java.util.Collection
import java.util.logging.{Level, LogManager, Logger}
import ba.sake.flatmark.FlatmarkGenerator
import ba.sake.flatmark.selenium.WebDriverHolder
import ba.sake.flatmark.swebserver.SWebServerHandler
import ba.sake.sharaf.undertow.handlers.RoutesHandler
import io.undertow.Undertow
import io.undertow.server.handlers.BlockingHandler
import io.undertow.server.handlers.resource.{
  PathResourceManager,
  ResourceChangeEvent,
  ResourceChangeListener,
  ResourceHandler
}

class FlatmarkCli(siteRootFolder: os.Path, port: Int, logLevel: Level, useCache: Boolean) {
  private val logger = Logger.getLogger(getClass.getName)

  def build(): Unit = {
    // set logging properties
    LogManager.getLogManager.readConfiguration(getClass.getClassLoader.getResource("logging.properties").openStream())
    LogManager.getLogManager.getLogger("").setLevel(logLevel) // set root logger level
    logger.info("Flatmark started")
    val startAtMillis = System.currentTimeMillis()
    val webDriverHolder = WebDriverHolder()
    val flatmarkServer = startFlatmarkServer(port)
    val generator = FlatmarkGenerator(port, webDriverHolder)
    try generator.generate(siteRootFolder, useCache)
    finally
      webDriverHolder.close()
      flatmarkServer.stop()
    val finishAtMillis = System.currentTimeMillis()
    val totalSeconds = (finishAtMillis - startAtMillis).toDouble / 1000
    logger.info(s"Flatmark finished in ${totalSeconds} s")
  }

  def serve(): Unit = {
    // set logging properties
    LogManager.getLogManager.readConfiguration(getClass.getClassLoader.getResource("logging.properties").openStream())
    LogManager.getLogManager.getLogger("").setLevel(logLevel) // set root logger level
    logger.info("Flatmark started")
    val webDriverHolder = WebDriverHolder()
    startFlatmarkServer(port)
    val generator = FlatmarkGenerator(port, webDriverHolder)
    generator.generate(siteRootFolder, useCache)
    os.watch.watch(
      Seq(siteRootFolder),
      changed => {
        val relevantFiles = changed.filterNot(p =>
          p.startsWith(siteRootFolder / ".flatmark-cache") || p.startsWith(siteRootFolder / "_site")
        )
        if relevantFiles.nonEmpty then {
          logger.info(s"Detected changes, regenerating..")
          generator.generate(siteRootFolder, useCache)
        }
      }
    )
  }

  private def startFlatmarkServer(port: Int): Undertow =
    logger.fine("Flatmark server starting...")
    val flatmarkRoutes = ba.sake.flatmark.ssr.routes
    val generatedSiteFolder = (siteRootFolder / "_site").wrapped
    val resourceManager = new PathResourceManager(generatedSiteFolder)
    val undertowHandler = BlockingHandler(
      RoutesHandler(
        flatmarkRoutes,
        SWebServerHandler(generatedSiteFolder, new ResourceHandler(resourceManager))
      )
    )
    val server = Undertow
      .builder()
      .addHttpListener(port, "localhost")
      .setHandler(undertowHandler)
      .build()
    server.start()
    logger.info(s"Flatmark server started at http://localhost:${port}")
    server
}
