package ba.sake.flatmark.cli

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

import java.time.Instant
import java.util.concurrent.atomic.AtomicReference

class FlatmarkCli(siteRootFolder: os.Path, host: String, port: Int, useCache: Boolean) {
  private val logger = LoggerFactory.getLogger(getClass.getName)

  def build(): Unit = {
    
    logger.info("Flatmark build started")
    val startAtMillis = System.currentTimeMillis()
    val webDriverHolder = WebDriverHolder()
    val ssrServerPort = NetworkUtils.getFreePort()
    val ssrServerUrl = s"http://localhost:${ssrServerPort}"
    val flatmarkSsrServer = startFlatmarkSsrServer(ssrServerPort)
    val generator = FlatmarkGenerator(ssrServerUrl, webDriverHolder)
    try generator.generate(siteRootFolder, useCache)
    finally
      flatmarkSsrServer.stop()
      webDriverHolder.close()
    val finishAtMillis = System.currentTimeMillis()
    val totalSeconds = (finishAtMillis - startAtMillis).toDouble / 1000
    logger.info(s"Flatmark build finished in ${totalSeconds} s")
  }

  def serve(): Unit = {
    logger.info("Flatmark serve started")
    val webDriverHolder = WebDriverHolder()
    val flatmarkServer = startFlatmarkServer()
    val ssrServerPort = NetworkUtils.getFreePort()
    val ssrServerUrl = s"http://localhost:${ssrServerPort}"
    val flatmarkSsrServer = startFlatmarkSsrServer(ssrServerPort)
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
    Runtime.getRuntime.addShutdownHook(new Thread(() => {
      flatmarkSsrServer.stop()
      flatmarkServer.stop()
      webDriverHolder.close()
    }))
  }

  private def startFlatmarkServer(): Undertow = {
    logger.debug("Flatmark server starting...")
    val generatedSiteFolder = siteRootFolder / "_site"
    if !os.exists(generatedSiteFolder) then os.makeDir(generatedSiteFolder)
    val resourceManager = PathResourceManager(generatedSiteFolder.wrapped)
    val fallbackResourceHandler = ResourceHandler(resourceManager).setDirectoryListingEnabled(true)
    val fileHandler = SwebserverFileHandler(generatedSiteFolder, host, port, fallbackResourceHandler)
    val lastChangeAt = new AtomicReference(Instant.now())
    val websocketHandler = Handlers.websocket(SwebserverWebSocketConnectionCallback(lastChangeAt))
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
      _ => lastChangeAt.set(Instant.now())
    )
    logger.info(s"Flatmark server started at http://${host}:${port}")
    server
  }

  private def startFlatmarkSsrServer(port: Int): UndertowSharafServer = {
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
}
