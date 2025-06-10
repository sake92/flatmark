package ba.sake.flatmark.selenium

import java.util.logging.{Level, Logger}
import org.openqa.selenium.chrome.{ChromeDriver, ChromeOptions}
import org.openqa.selenium.logging.{LogType, LoggingPreferences}
import org.openqa.selenium.remote.RemoteWebDriver

class WebDriverHolder {
  private val logger = Logger.getLogger(getClass.getName)

  @volatile private var initialized = false

  // selenium automatically downloads the driver binaries and webdriver!
  lazy val driver: RemoteWebDriver = {
    logger.fine("WebDriver starting...")
    val chromeOptions = new ChromeOptions()
    val driverLogPrefs = new LoggingPreferences()
    driverLogPrefs.enable(LogType.BROWSER, Level.ALL)
    chromeOptions.setCapability("goog:loggingPrefs", driverLogPrefs)
    chromeOptions.addArguments("--headless=new")
    chromeOptions.addArguments("--allow-file-access-from-files")
    val webDriver = new ChromeDriver(chromeOptions)
    initialized = true
    logger.fine("WebDriver started")
    webDriver
  }

  def close(): Unit = {
    if initialized then {
      logger.fine("Closing WebDriver...")
      driver.quit()
      initialized = false
      logger.fine("WebDriver closed")
    } else {
      logger.fine("WebDriver was not initialized, nothing to close.")
    }
  }
}
