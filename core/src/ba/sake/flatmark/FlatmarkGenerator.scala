package ba.sake.flatmark

import java.util as ju
import java.util.logging.Logger
import scala.util.boundary
import scala.jdk.CollectionConverters.*
import org.yaml.snakeyaml.Yaml

class FlatmarkGenerator(port: Int, chromeDriverHolder: ChromeDriverHolder) {
  private val logger = Logger.getLogger(getClass.getName)

  private val yaml = new Yaml()

  def generate(siteRootFolder: os.Path, useCache: Boolean): Unit = {

    val siteConfigYaml = os.read(siteRootFolder / "_config.yaml")
    val siteConfigFileMap = yaml.load[ju.Map[String, String]](siteConfigYaml).asScala.toMap
    val siteConfig = SiteConfig.fromMap(siteConfigFileMap)

    val cacheFolder = siteRootFolder / ".flatmark-cache"
    val fileCache = FileCache(cacheFolder, useCache)

    val codeHighlighter = FlatmarkCodeHighlighter(port, chromeDriverHolder, fileCache)
    val graphvizRenderer = FlatmarkGraphvizRenderer(port, chromeDriverHolder, fileCache)
    val mathRenderer = FlatmarkMathRenderer(port, chromeDriverHolder, fileCache)
    val markdownRenderer = FlatmarkMarkdownRenderer(codeHighlighter, graphvizRenderer, mathRenderer)
    val templateHandler = FlatmarkTemplateHandler()

    val layoutsFolder = siteRootFolder / "_layouts"
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

    val outputFolder = siteRootFolder / "_site"
    os.walk(siteRootFolder, skip = shouldSkip).foreach { file =>
      if file.ext == "md" then {
        renderMarkdownFile(
          siteConfig,
          siteRootFolder,
          file,
          outputFolder,
          layoutTemplatesMap,
          markdownRenderer,
          templateHandler
        )
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
      siteConfig: SiteConfig,
      siteRootFolder: os.Path,
      file: os.Path,
      outputFolder: os.Path,
      layoutTemplatesMap: Map[String, String],
      markdownRenderer: FlatmarkMarkdownRenderer,
      templateHandler: FlatmarkTemplateHandler
  ): Unit = {
    logger.fine(s"Markdown file rendering: ${file}")
    val mdContentTemplateRaw = os.read(file)
    val (mdContentTemplate, pageConfig) = parseMd(file.baseName, mdContentTemplateRaw)
    val templateConfig = TemplateConfig(siteConfig, pageConfig)
    val mdContent = templateHandler.render(file.baseName, mdContentTemplate, templateConfig.toJavaMap)
    val mdHtmlContent = markdownRenderer.renderMarkdown(mdContent)
    // render final HTML file
    val layoutTemplate = layoutTemplatesMap.getOrElse(
      pageConfig.layout,
      throw RuntimeException(s"Layout '${pageConfig.layout}' not found for file: ${file}")
    )
    val htmlContent = templateHandler.render(
      pageConfig.layout,
      layoutTemplate,
      templateConfig.copy(page = pageConfig.copy(content = mdHtmlContent)).toJavaMap
    )
    os.write.over(
      outputFolder / file.relativeTo(siteRootFolder).segments.init / s"${file.baseName}.html",
      htmlContent,
      createFolders = true
    )
    logger.fine(s"Markdown file rendered: ${file}")
  }

  private def parseMd(fileNameBase: String, mdTemplateRaw: String): (String, PageConfig) = {
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
    val (mdContentTemplate, yamlMap) = if hasYamlFrontMatter then {
      val t = mdTemplateRaw.linesIterator
        .drop(secondTripleDashIndex + 1)
        .mkString("\n")
        .trim
      val rawYaml = mdTemplateRaw.linesIterator
        .slice(firstTripleDashIndex + 1, firstTripleDashIndex + 1 + secondTripleDashIndex - firstTripleDashIndex - 1)
        .mkString("\n")
      (t, yaml.load[ju.Map[String, String]](rawYaml).asScala.toMap)
    } else {
      (mdTemplateRaw, Map("title" -> fileNameBase))
    }
    (mdContentTemplate, PageConfig.fromMap(yamlMap))
  }

}

case class TemplateConfig(site: SiteConfig, page: PageConfig) {
  def toJavaMap: ju.Map[String, Object] = Map(
    "site" -> site,
    "page" -> page
  ).asJava
}

case class SiteConfig(name: String, description: String) {
  def toJavaMap: ju.Map[String, Object] = Map(
    "name" -> name,
    "description" -> description
  ).asJava
}

object SiteConfig:
  def fromMap(map: Map[String, String]): SiteConfig =
    SiteConfig(
      name = map.getOrElse("name", "My Site"),
      description = map.getOrElse("description", "")
    )

case class PageConfig(layout: String, title: String, description: String, content: String) {
  def toJavaMap: ju.Map[String, Object] = Map(
    "layout" -> layout,
    "title" -> title,
    "description" -> description,
    "content" -> content
  ).asJava
}

object PageConfig:
  def fromMap(map: Map[String, String]): PageConfig =
    PageConfig(
      layout = map.getOrElse("layout", "default"),
      title = map.getOrElse("title", "Untitled"),
      description = map.getOrElse("description", ""),
      content = map.getOrElse("content", "")
    )
