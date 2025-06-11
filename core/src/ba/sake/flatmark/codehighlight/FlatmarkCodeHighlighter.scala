package ba.sake.flatmark.codehighlight

import java.net.URLEncoder
import java.time.Duration
import java.util.logging.{Level, Logger}
import scala.jdk.CollectionConverters.*
import org.openqa.selenium.By
import org.openqa.selenium.logging.LogType
import org.openqa.selenium.support.ui.WebDriverWait
import ba.sake.flatmark.FileCache
import ba.sake.flatmark.selenium.WebDriverHolder

// driver is lazy because of performance reasons, it is created only when needed
class FlatmarkCodeHighlighter(ssrServerPort: Int, webDriverHolder: WebDriverHolder, fileCache: FileCache) {

  private val logger = Logger.getLogger(getClass.getName)

  def highlight(codeStr: String, codeLang: Option[String] = None): String =
    fileCache.cached("highlightjs", codeStr, codeLang.getOrElse("plaintext")) {
      try {
        logger.fine("Highlighting code start")
        val encodedCodeStr = URLEncoder.encode(codeStr, "utf-8")
        val encodedCodeLang = codeLang.map(lang => URLEncoder.encode(lang, "utf-8")).getOrElse("plaintext")
        val url = s"http://localhost:${ssrServerPort}/ssr/highlightjs?code=${encodedCodeStr}&lang=${encodedCodeLang}"
        webDriverHolder.driver.get(url)
        val waitCondition = new WebDriverWait(webDriverHolder.driver, Duration.ofSeconds(5))
        waitCondition.until(_ => webDriverHolder.driver.executeScript("return renderFinished;") == true)
        webDriverHolder.driver.findElement(By.id("result")).getDomProperty("innerHTML")
      } catch {
        case e: org.openqa.selenium.WebDriverException =>
          val logs = webDriverHolder.driver.manage().logs().get(LogType.BROWSER).getAll
          logger.log(Level.SEVERE, s"Errors during code highlighting: ${logs.asScala.mkString("\n")}", e)
          codeStr
      }
    }

}
