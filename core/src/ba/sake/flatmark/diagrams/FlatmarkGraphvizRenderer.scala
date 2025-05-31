package ba.sake.flatmark.diagrams

import java.net.URLEncoder
import java.time.Duration
import java.util.logging.{Level, Logger}
import scala.jdk.CollectionConverters.*
import org.openqa.selenium.By
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.logging.LogType
import org.openqa.selenium.support.ui.WebDriverWait
import ba.sake.flatmark.FileCache
import ba.sake.flatmark.selenium.ChromeDriverHolder

class FlatmarkGraphvizRenderer(port:Int,driverHolder: ChromeDriverHolder, fileCache: FileCache) {
  private val logger = Logger.getLogger(getClass.getName)

  def render(dotStr: String, engine: String = "dot"): String =
    fileCache.cached(dotStr, engine) {
      try {
        logger.fine("Render graphviz start")
        val encodedDotStr = URLEncoder.encode(dotStr, "utf-8")
        val encodedEngine = URLEncoder.encode(engine, "utf-8")
        val url = s"http://localhost:${port}/ssr/graphviz?source=${encodedDotStr}&engine=${encodedEngine}"
        driverHolder.driver.get(url)
        val waitCondition = new WebDriverWait(driverHolder.driver, Duration.ofSeconds(5))
        waitCondition.until(_ => driverHolder.driver.executeScript("return renderFinished;") == true)
        driverHolder.driver.findElement(By.id("result")).getDomProperty("innerHTML")
      } catch {
        case e: org.openqa.selenium.WebDriverException =>
          val logs = driverHolder.driver.manage().logs().get(LogType.BROWSER).getAll
          logger.log(Level.SEVERE, s"Errors during code highlighting: ${logs.asScala.mkString("\n")}", e)
          dotStr
      }
    }

}
