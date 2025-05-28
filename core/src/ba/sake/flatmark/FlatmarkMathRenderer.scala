package ba.sake.flatmark

import java.net.URLEncoder
import java.time.Duration
import scala.jdk.CollectionConverters.*
import org.openqa.selenium.By
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.logging.LogType
import org.openqa.selenium.support.ui.WebDriverWait

import java.util.logging.{Level, Logger}

class FlatmarkMathRenderer(port:Int, driverHolder: ChromeDriverHolder, fileCache: FileCache) {
  private val logger = Logger.getLogger(getClass.getName)

  def render(mathStr: String): String = fileCache.cached(mathStr) {
    try {
      logger.fine("Render math start")
      val encodedMathStr = URLEncoder.encode(mathStr, "utf-8")
      val url = s"http://localhost:${port}/ssr/katex?source=${encodedMathStr}"
      driverHolder.driver.get(url)
      val waitCondition = new WebDriverWait(driverHolder.driver, Duration.ofSeconds(5))
      waitCondition.until(_ => driverHolder.driver.executeScript("return renderFinished;") == true)
      driverHolder.driver.findElement(By.id("result")).getDomProperty("innerHTML")
    } catch {
      case e: org.openqa.selenium.WebDriverException =>
        val logs = driverHolder.driver.manage().logs().get(LogType.BROWSER).getAll()
        logger.log(Level.SEVERE, s"Errors during code highlighting: ${logs.asScala.mkString("\n")}", e)
        mathStr
    }
  }

}
