package ba.sake.flatmark.diagrams

import java.net.URLEncoder
import java.time.Duration
import scala.jdk.CollectionConverters.*
import org.slf4j.LoggerFactory
import org.openqa.selenium.By
import org.openqa.selenium.logging.LogType
import org.openqa.selenium.support.ui.WebDriverWait
import ba.sake.flatmark.FileCache
import ba.sake.flatmark.selenium.WebDriverHolder

class FlatmarkMermaidRenderer(ssrServerUrl: String, webDriverHolder: WebDriverHolder, fileCache: FileCache) {
  
  private val logger = LoggerFactory.getLogger(getClass.getName)

  def render(source: String): String =
    fileCache.cached("mermaid", source) {
      try {
        logger.debug("Render mermaid start")
        val encodedSource = URLEncoder.encode(source, "utf-8")
        val url = s"${ssrServerUrl}/ssr/mermaid?source=${encodedSource}"
        webDriverHolder.driver.get(url)
        val waitCondition = new WebDriverWait(webDriverHolder.driver, Duration.ofSeconds(5))
        waitCondition.until(_ => webDriverHolder.driver.executeScript("return renderFinished;") == true)
        webDriverHolder.driver.findElement(By.id("result")).getDomProperty("innerHTML")
      } catch {
        case e: org.openqa.selenium.WebDriverException =>
          val logs = webDriverHolder.driver.manage().logs().get(LogType.BROWSER).getAll
          logger.error(s"Errors during mermaid rendering: ${logs.asScala.mkString("\n")}", e)
          source
      }
    }

}
