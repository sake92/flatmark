package ba.sake.flatmark.cli

import java.util.logging.{Level, LogManager, Logger}
import ba.sake.flatmark.FlatmarkGenerator
import ba.sake.flatmark.selenium.WebDriverHolder
import ba.sake.flatmark.swebserver.SWebServerHandler
import ba.sake.sharaf.undertow.UndertowSharafServer
import ba.sake.sharaf.undertow.handlers.RoutesHandler
import ba.sake.sharaf.utils.NetworkUtils
import io.undertow.Undertow
import io.undertow.server.handlers.BlockingHandler
import io.undertow.server.handlers.resource.{PathResourceManager, ResourceHandler}

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
    val ssrServerPort = NetworkUtils.getFreePort()
    val flatmarkSsrServer = startFlatmarkSsrServer(ssrServerPort)
    val generator = FlatmarkGenerator(ssrServerPort, webDriverHolder)
    try generator.generate(siteRootFolder, useCache)
    finally
      webDriverHolder.close()
      flatmarkServer.stop()
      flatmarkSsrServer.stop()
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
    val ssrServerPort = NetworkUtils.getFreePort()
    startFlatmarkSsrServer(ssrServerPort)
    val generator = FlatmarkGenerator(ssrServerPort, webDriverHolder)
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
    val generatedSiteFolder = (siteRootFolder / "_site").wrapped
    val resourceManager = new PathResourceManager(generatedSiteFolder)
    val undertowHandler = SWebServerHandler(generatedSiteFolder, new ResourceHandler(resourceManager))
    val server = Undertow
      .builder()
      .addHttpListener(port, "localhost")
      .setHandler(undertowHandler)
      .build()
    server.start()
    logger.info(s"Flatmark server started at http://localhost:${port}")
    server

  private def startFlatmarkSsrServer(port: Int): UndertowSharafServer =
    logger.fine("Flatmark SSR server starting...")
    val server = UndertowSharafServer(
      "localhost",
      port,
      ba.sake.flatmark.ssr.routes
    )
    server.start()
    logger.fine(s"Flatmark SSR server started at http://localhost:${port}")
    server
}
