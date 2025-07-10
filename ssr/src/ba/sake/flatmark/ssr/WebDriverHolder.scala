package ba.sake.flatmark.ssr

import java.util.logging.{Level, Logger}
import org.slf4j.LoggerFactory
import org.openqa.selenium.chrome.{ChromeDriver, ChromeOptions}
import org.openqa.selenium.logging.{LogType, LoggingPreferences}
import org.openqa.selenium.remote.RemoteWebDriver

class WebDriverHolder {
  private val logger = LoggerFactory.getLogger(getClass.getName)

  @volatile private var initialized = false

  // selenium automatically downloads the driver binaries and webdriver!
  lazy val driver: RemoteWebDriver = {
    logger.debug("WebDriver starting...")
    val chromeOptions = new ChromeOptions()
    val driverLogPrefs = new LoggingPreferences()
    driverLogPrefs.enable(LogType.BROWSER, Level.ALL)
    chromeOptions.setCapability("goog:loggingPrefs", driverLogPrefs)
    chromeOptions.addArguments("--headless=new")
    chromeOptions.setBrowserVersion("138")
    val webDriver = new ChromeDriver(chromeOptions)
    initialized = true
    logger.debug("WebDriver started")
    webDriver
  }

  def quit(): Unit = {
    if initialized then {
      logger.debug("Quitting WebDriver...")
      driver.quit()
      initialized = false
      logger.debug("WebDriver quitted")
    } else {
      logger.debug("WebDriver was not initialized, nothing to quit.")
    }
  }
}
