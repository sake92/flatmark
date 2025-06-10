package ba.sake.flatmark.diagrams

import java.net.URLEncoder
import java.time.Duration
import java.util.logging.{Level, Logger}
import scala.jdk.CollectionConverters.*
import org.openqa.selenium.By
import org.openqa.selenium.logging.LogType
import org.openqa.selenium.support.ui.WebDriverWait
import ba.sake.flatmark.FileCache
import ba.sake.flatmark.selenium.WebDriverHolder

class FlatmarkGraphvizRenderer(port: Int, webDriverHolder: WebDriverHolder, fileCache: FileCache) {
  private val logger = Logger.getLogger(getClass.getName)

  def render(dotStr: String, engine: String = "dot"): String =
    fileCache.cached(dotStr, engine) {
      try {
        logger.fine("Render graphviz start")
        val encodedDotStr = URLEncoder.encode(dotStr, "utf-8")
        val encodedEngine = URLEncoder.encode(engine, "utf-8")
        val url = s"http://localhost:${port}/ssr/graphviz?source=${encodedDotStr}&engine=${encodedEngine}"
        webDriverHolder.driver.get(url)
        val waitCondition = new WebDriverWait(webDriverHolder.driver, Duration.ofSeconds(5))
        waitCondition.until(_ => webDriverHolder.driver.executeScript("return renderFinished;") == true)
        webDriverHolder.driver.findElement(By.id("result")).getDomProperty("innerHTML")
      } catch {
        case e: org.openqa.selenium.WebDriverException =>
          val logs = webDriverHolder.driver.manage().logs().get(LogType.BROWSER).getAll
          logger.log(Level.SEVERE, s"Errors during code highlighting: ${logs.asScala.mkString("\n")}", e)
          dotStr
      }
    }

}
