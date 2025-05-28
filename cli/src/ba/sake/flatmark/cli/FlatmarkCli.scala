package ba.sake.flatmark.cli

import java.util.logging.{Level, Logger}
import java.util.logging.LogManager
import scala.jdk.CollectionConverters.*
import org.openqa.selenium.chrome.{ChromeDriver, ChromeOptions}
import org.openqa.selenium.logging.{LogType, LoggingPreferences}
import ba.sake.flatmark.FlatmarkGenerator
import ba.sake.sharaf.undertow.UndertowSharafServer

class FlatmarkCli {
  private val logger = Logger.getLogger(getClass.getName)

  def run(siteRootFolder: os.Path): Unit = {
    // set logging properties
    LogManager.getLogManager.readConfiguration(getClass.getClassLoader.getResource("logging.properties").openStream())
    
    logger.info("Flatmark started")
    
    val startAtMillis = System.currentTimeMillis()
    val chromeDriver = startChromeDriver()
    val flatmarkServer = startFlatmarkServer()
    val generator = FlatmarkGenerator(chromeDriver)
    try generator.generate(siteRootFolder)
    finally
      chromeDriver.quit()
      flatmarkServer.stop()
    val finishAtMillis = System.currentTimeMillis()
    val totalSeconds = (finishAtMillis - startAtMillis).toDouble / 1000
    logger.info(s"Flatmark finished in ${totalSeconds} s")
  }

  private def startChromeDriver(): ChromeDriver =
    logger.fine("ChromeDriver starting...")
    val chromeOptions = new ChromeOptions()
    val driverLogPrefs = new LoggingPreferences()
    driverLogPrefs.enable(LogType.BROWSER, Level.ALL)
    chromeOptions.setCapability("goog:loggingPrefs", driverLogPrefs)
    chromeOptions.addArguments("--headless=new")
    chromeOptions.addArguments("--allow-file-access-from-files")
    val chromeDriver = new ChromeDriver(chromeOptions)
    logger.fine("ChromeDriver started")
    chromeDriver

  private def startFlatmarkServer(): UndertowSharafServer =
    logger.fine("Flatmark server starting...")
    val flatmarkServerRoutes = ba.sake.flatmark.ssr.routes
    val server = UndertowSharafServer("localhost", 8181, flatmarkServerRoutes)
    server.start()
    logger.fine("Flatmark server started")
    server
}
