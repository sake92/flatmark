package ba.sake.flatmark

import java.net.URLEncoder
import java.time.Duration
import scala.jdk.CollectionConverters.*
import org.openqa.selenium.By
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.logging.LogType
import org.openqa.selenium.support.ui.WebDriverWait

import java.util.logging.{Level, Logger}

class FlatmarkCodeHighlighter(driver: ChromeDriver, fileCache: FileCache) {
  
  private val logger = Logger.getLogger(getClass.getName)

  def highlight(codeStr: String, codeLang: Option[String] = None): String = try {
    logger.fine("Highlighting code start")
    val encodedCodeStr = URLEncoder.encode(codeStr, "utf-8")
    val encodedCodeLang = codeLang.map(lang => URLEncoder.encode(lang, "utf-8")).getOrElse("plaintext")
    val url = s"http://localhost:8181/ssr/highlightjs?code=${encodedCodeStr}&lang=${encodedCodeLang}"
    driver.get(url)
    val waitCondition = new WebDriverWait(driver, Duration.ofSeconds(5))
    waitCondition.until(_ => driver.executeScript("return renderFinished;") == true)
    driver.findElement(By.id("result")).getDomProperty("innerHTML")
  } catch {
    case e: org.openqa.selenium.WebDriverException =>
      val logs = driver.manage().logs().get(LogType.BROWSER).getAll()
      logger.log(Level.SEVERE, s"Errors during code highlighting: ${logs.asScala.mkString("\n")}", e)
      codeStr
  }

}
