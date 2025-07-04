package ba.sake.flatmark.codehighlight

import java.net.URLEncoder
import java.time.Duration
import scala.jdk.CollectionConverters.*
import org.slf4j.LoggerFactory
import org.openqa.selenium.By
import org.openqa.selenium.logging.LogType
import org.openqa.selenium.support.ui.WebDriverWait
import ba.sake.flatmark.FileCache
import ba.sake.flatmark.selenium.WebDriverHolder

// driver is lazy because of performance reasons, it is created only when needed
class FlatmarkCodeHighlighter(ssrServerUrl: String, webDriverHolder: WebDriverHolder, fileCache: FileCache) {

  private val logger = LoggerFactory.getLogger(getClass.getName)

  def highlight(codeStr: String, codeLang: Option[String] = None): String =
    fileCache.cached("highlightjs", codeStr, codeLang.getOrElse("plaintext")) {
      try {
        logger.debug("Highlighting code start")
        val encodedCodeStr = URLEncoder.encode(codeStr, "utf-8")
        val encodedCodeLang = codeLang.map(lang => URLEncoder.encode(lang, "utf-8")).getOrElse("plaintext")
        val url = s"${ssrServerUrl}/ssr/highlightjs?code=${encodedCodeStr}&lang=${encodedCodeLang}"
        webDriverHolder.driver.get(url)
        val waitCondition = new WebDriverWait(webDriverHolder.driver, Duration.ofSeconds(5))
        waitCondition.until(_ => webDriverHolder.driver.executeScript("return renderFinished;") == true)
        webDriverHolder.driver.findElement(By.id("result")).getDomProperty("innerHTML")
      } catch {
        case e: org.openqa.selenium.WebDriverException =>
          val logs = webDriverHolder.driver.manage().logs().get(LogType.BROWSER).getAll
          logger.error(s"Errors during code highlighting: ${logs.asScala.mkString("\n")}", e)
          codeStr
        case e: org.openqa.selenium.JavascriptException =>
          val logs = webDriverHolder.driver.manage().logs().get(LogType.BROWSER).getAll
          logger.error(s"Errors during code highlighting: ${logs.asScala.mkString("\n")}", e)
          codeStr
      }
    }

}
