package ba.sake.flatmark

import java.util as ju
import java.util.logging.Logger
import scala.util.boundary
import scala.jdk.CollectionConverters.*
import org.openqa.selenium.chrome.ChromeDriver
import org.yaml.snakeyaml.Yaml

class FlatmarkGenerator(port: Int, chromeDriverHolder: ChromeDriverHolder) {
  private val logger = Logger.getLogger(getClass.getName)
  
  private val yaml = new Yaml()

  def generate(siteRootFolder: os.Path, useCache: Boolean): Unit = {

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

    // TODO custom config
    // skip any file/folder that starts with . or _
    def shouldSkip(file: os.Path) =
      file.segments.exists(s => s.startsWith(".") || s.startsWith("_"))

    os.walk(siteRootFolder, skip = shouldSkip).foreach { file =>
      if file.ext == "md" then {
        renderMarkdownFile(siteRootFolder, file, outputFolder, layoutTemplatesMap, markdownRenderer, templateHandler)
      } else if os.isFile(file) then {
        os.copy(
          file,
          outputFolder / file.relativeTo(siteRootFolder),
          replaceExisting = true,
          createFolders = true,
          mergeFolders = true,
          followLinks = false
        )
      }
    }
  }

  private def renderMarkdownFile(
      siteRootFolder: os.Path,
      file: os.Path,
      outputFolder: os.Path,
      layoutTemplatesMap: Map[String, String],
      markdownRenderer: FlatmarkMarkdownRenderer,
      templateHandler: FlatmarkTemplateHandler
  ): Unit = {
    logger.fine(s"Markdown file rendering: ${file}")
    val mdContentTemplateRaw = os.read(file)
    val (mdContentTemplate, pageData) = parseMd(mdContentTemplateRaw)
    // TODO parse site data
    val templateData = TemplateData(SiteData.Default, pageData)
    val mdContent = templateHandler.render(file.baseName, mdContentTemplate, templateData.toJavaMap)
    val mdHtmlContent = markdownRenderer.renderMarkdown(mdContent)
    // render final HTML file
    val layoutTemplate = layoutTemplatesMap("default")
    val htmlContent = templateHandler.render(
      "default",
      layoutTemplate,
      templateData.copy(page = pageData.copy(content = mdHtmlContent)).toJavaMap
    )
    os.write.over(
      outputFolder / file.relativeTo(siteRootFolder).segments.init / s"${file.baseName}.html",
      htmlContent,
      createFolders = true
    )
    logger.fine(s"Markdown file rendered: ${file}")
  }

  private def parseMd(mdTemplateRaw: String): (String, PageData) = {
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
    val (mdContentTemplate, yamlMap) =if hasYamlFrontMatter then {
      val t = mdTemplateRaw.linesIterator
        .drop(secondTripleDashIndex + 1)
        .mkString("\n")
        .trim
      val rawYaml = mdTemplateRaw.linesIterator
        .slice(firstTripleDashIndex + 1, firstTripleDashIndex + 1 + secondTripleDashIndex - firstTripleDashIndex - 1)
        .mkString("\n")
      (t, yaml.load[ju.Map[String, String]](rawYaml).asScala.toMap)
    } else {
      (mdTemplateRaw, Map.empty[String, String])
    }
    
    (mdContentTemplate, PageData.fromMap(yamlMap))
  }

}

case class TemplateData(site: SiteData, page: PageData)  {
  def toJavaMap: ju.Map[String, Object] = Map(
    "site" -> site,
    "page" -> page
  ).asJava
}

case class SiteData(title: String, description: String) {
  def toJavaMap: ju.Map[String, Object] = Map(
    "title" -> title,
    "description" -> description
  ).asJava
}

object SiteData:
  val Default: SiteData = SiteData("Untitled", "No description available")

case class PageData(title: String, description: String, content: String) {
  def toJavaMap: ju.Map[String, Object] = Map(
    "title" -> title,
    "page" -> description,
    "content" -> content
  ).asJava
}

object PageData:
  def fromMap(map: Map[String, String]): PageData = {
    PageData(
      title = map.getOrElse("title", "Untitled"),
      description = map.getOrElse("description", "No description available"),
      content = map.getOrElse("content", "")
    )
  }
