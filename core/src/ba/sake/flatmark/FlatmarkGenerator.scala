package ba.sake.flatmark

import org.openqa.selenium.chrome.ChromeDriver

import scala.util.boundary
import org.virtuslab.yaml.*

import java.util.logging.Logger

class FlatmarkGenerator(port: Int, chromeDriverHolder: ChromeDriverHolder) {
  private val logger = Logger.getLogger(getClass.getName)

  def generate(siteRootFolder: os.Path, useCache: Boolean): Unit = {

    // TODO make sure these exist
    val outputFolder = siteRootFolder / "_site"
    val layoutsFolder = siteRootFolder / "_layouts"

    val cacheFolder = siteRootFolder / ".flatmark-cache"
    val fileCache = FileCache(cacheFolder, useCache)

    val codeHighlighter = FlatmarkCodeHighlighter(port, chromeDriverHolder, fileCache)
    val graphvizRenderer = FlatmarkGraphvizRenderer(port, chromeDriverHolder, fileCache)
    val mathRenderer = FlatmarkMathRenderer(port, chromeDriverHolder, fileCache)
    val markdownRenderer = FlatmarkMarkdownRenderer(codeHighlighter, graphvizRenderer, mathRenderer)
    val templateHandler = FlatmarkTemplateHandler()

    val layoutTemplatesMap =
      if os.exists(layoutsFolder) then
        os
          .list(layoutsFolder)
          .filter(_.ext == "html")
          .map { file =>
            val layoutContent = os.read(file)
            file.baseName -> layoutContent
          }
          .toMap
      else Map()

    os.walk(siteRootFolder).filter(_.ext == "md").foreach { file =>
      // render markdown content
      logger.fine(s"rendering markdown file: ${file}")
      val mdContentTemplateRaw = os.read(file)
      val (mdContentTemplate, frontMatter) = parseMd(mdContentTemplateRaw)
      val mdContent = templateHandler.render(file.baseName, mdContentTemplate, frontMatter)
      val mdHtmlContent = markdownRenderer.renderMarkdown(mdContent)
      // render final HTML file
      val layoutTemplate = layoutTemplatesMap("default")
      val htmlContent =
        templateHandler
          .render("default", layoutTemplate, Map("title" -> "Flatmark Example", "content" -> mdHtmlContent))
      os.write.over(
        outputFolder / file.relativeTo(siteRootFolder).segments.init / s"${file.baseName}.html",
        htmlContent,
        createFolders = true
      )
      logger.fine(s"markdown file rendered: ${file}")
    }
  }

  private def parseMd(mdTemplateRaw: String): (String, Map[String, String]) = {
    var hasYamlFrontMatter = false
    var firstTripleDashIndex = -1
    var secondTripleDashIndex = -1
    boundary {
      val iter = mdTemplateRaw.linesIterator
      var i = 0
      while iter.hasNext do {
        val line = iter.next().trim
        if line.nonEmpty then {
          if line == "---" then {
            if (firstTripleDashIndex == -1) firstTripleDashIndex = i
            else if (secondTripleDashIndex == -1) {
              secondTripleDashIndex = i
              hasYamlFrontMatter = true
              boundary.break()
            }
          } else if (firstTripleDashIndex == -1) {
            boundary.break() // first non-empty line is not triple dash -> no YAML front matter
          }
        }
        i += 1
      }
    }
    if hasYamlFrontMatter then {
      val t = mdTemplateRaw.linesIterator
        .drop(secondTripleDashIndex + 1)
        .mkString("\n")
        .trim
      val fm = mdTemplateRaw.linesIterator
        .slice(firstTripleDashIndex + 1, firstTripleDashIndex + 1 + secondTripleDashIndex - firstTripleDashIndex - 1)
        .mkString("\n")
        .as[Map[String, String]]
        .toOption
        .getOrElse(Map())
      (t, fm)
    } else {
      (mdTemplateRaw, Map())
    }
  }

}
