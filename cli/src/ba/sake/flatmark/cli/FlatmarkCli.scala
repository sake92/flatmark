package ba.sake.flatmark.cli

import java.util.logging.{Level, LogManager}
import org.slf4j.LoggerFactory
import io.undertow.Undertow
import io.undertow.server.handlers.resource.{PathResourceManager, ResourceHandler}
import io.undertow.Handlers
import ba.sake.flatmark.FlatmarkGenerator
import ba.sake.flatmark.selenium.WebDriverHolder
import ba.sake.sharaf.undertow.UndertowSharafServer
import ba.sake.sharaf.utils.NetworkUtils
import ba.sake.swebserver.SwebserverFileHandler
import ba.sake.swebserver.SwebserverWebSocketConnectionCallback

class FlatmarkCli(siteRootFolder: os.Path, host: String, port: Int, logLevel: Level, useCache: Boolean) {
  private val logger = LoggerFactory.getLogger(getClass.getName)

  def build(): Unit = {
    // set logging properties
    LogManager.getLogManager.readConfiguration(getClass.getClassLoader.getResource("logging.properties").openStream())
    LogManager.getLogManager.getLogger("").setLevel(logLevel) // set root logger level
    logger.info("Flatmark build started")
    val startAtMillis = System.currentTimeMillis()
    val webDriverHolder = WebDriverHolder()
    val ssrServerPort = NetworkUtils.getFreePort()
    val flatmarkSsrServer = startFlatmarkSsrServer(ssrServerPort)
    val generator = FlatmarkGenerator(ssrServerPort, webDriverHolder)
    try generator.generate(siteRootFolder, useCache)
    finally
      webDriverHolder.close()
      flatmarkSsrServer.stop()
    val finishAtMillis = System.currentTimeMillis()
    val totalSeconds = (finishAtMillis - startAtMillis).toDouble / 1000
    logger.info(s"Flatmark build finished in ${totalSeconds} s")
  }

  def serve(): Unit = {
    // set logging properties
    LogManager.getLogManager.readConfiguration(getClass.getClassLoader.getResource("logging.properties").openStream())
    LogManager.getLogManager.getLogger("").setLevel(logLevel) // set root logger level
    logger.info("Flatmark serve started")
    val webDriverHolder = WebDriverHolder()
    startFlatmarkServer(port)
    val ssrServerPort = NetworkUtils.getFreePort()
    val ssrServerUrl = s"http://localhost:${ssrServerPort}"
    startFlatmarkSsrServer(ssrServerPort)
    val generator = FlatmarkGenerator(ssrServerUrl, webDriverHolder)
    generator.generate(siteRootFolder, useCache)
    os.watch.watch(
      Seq(siteRootFolder),
      changed => {
        val relevantFiles = changed.filterNot(p =>
          p.startsWith(siteRootFolder / ".flatmark-cache") || p.startsWith(siteRootFolder / "_site")
        )
        if relevantFiles.nonEmpty then {
          logger.info(s"Detected changes, regenerating...")
          generator.generate(siteRootFolder, useCache)
        }
      }
    )
  }

  private def startFlatmarkServer(host: String, port: Int): Undertow =
    logger.debug("Flatmark server starting...")
    val generatedSiteFolder = siteRootFolder / "_site"
    if !os.exists(generatedSiteFolder) then os.makeDir(generatedSiteFolder)
    val resourceManager = PathResourceManager(generatedSiteFolder.wrapped)
    val fileHandler = SwebserverFileHandler(generatedSiteFolder, host, port, ResourceHandler(resourceManager))
    val changes = new java.util.concurrent.atomic.AtomicBoolean(false)
    val websocketHandler = Handlers.websocket(SwebserverWebSocketConnectionCallback(changes))
    val server = Undertow
      .builder()
      .addHttpListener(port, host)
      .setHandler(
        Handlers
          .path()
          .addPrefixPath("/ws", websocketHandler)
          .addPrefixPath("/", fileHandler)
      )
      .build()
    server.start()
    os.watch.watch(
      Seq(generatedSiteFolder),
      _ => changes.set(true)
    )
    logger.info(s"Flatmark server started at http://${host}:${port}")
    server

  private def startFlatmarkSsrServer(port: Int): UndertowSharafServer =
    logger.debug("Flatmark SSR server starting...")
    val server = UndertowSharafServer(
      "localhost",
      port,
      ba.sake.flatmark.ssr.routes
    )
    server.start()
    logger.debug(s"Flatmark SSR server started at http://localhost:${port}")
    server
}
