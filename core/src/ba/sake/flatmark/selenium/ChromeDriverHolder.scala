package ba.sake.flatmark.selenium

import org.openqa.selenium.chrome.{ChromeDriver, ChromeOptions}
import org.openqa.selenium.logging.{LogType, LoggingPreferences}

import java.util.logging.{Level, Logger}

class ChromeDriverHolder {
  private val logger = Logger.getLogger(getClass.getName)
  
  private var initialized = false
  
  lazy val driver: ChromeDriver = {
    logger.fine("ChromeDriver starting...")
    val chromeOptions = new ChromeOptions()
    val driverLogPrefs = new LoggingPreferences()
    driverLogPrefs.enable(LogType.BROWSER, Level.ALL)
    chromeOptions.setCapability("goog:loggingPrefs", driverLogPrefs)
    chromeOptions.addArguments("--headless=new")
    chromeOptions.addArguments("--allow-file-access-from-files")
    val chromeDriver = new ChromeDriver(chromeOptions)
    logger.fine("ChromeDriver started")
    initialized = true
    chromeDriver
  }

  def close(): Unit = {
    if (initialized) {
      logger.fine("Closing ChromeDriver...")
      driver.quit()
      initialized = false
      logger.fine("ChromeDriver closed")
    } else {
      logger.fine("ChromeDriver was not initialized, nothing to close.")
    }
  }
}
