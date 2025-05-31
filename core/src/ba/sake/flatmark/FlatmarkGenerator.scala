package ba.sake.flatmark

import java.util as ju
import java.util.logging.Logger
import scala.util.boundary
import scala.jdk.CollectionConverters.*
import org.virtuslab.yaml.*

class FlatmarkGenerator(port: Int, chromeDriverHolder: ChromeDriverHolder) {
  private val logger = Logger.getLogger(getClass.getName)

  def generate(siteRootFolder: os.Path, useCache: Boolean): Unit = {

    val siteConfigFile = siteRootFolder / "_config.yaml"
    val contentFolder = siteRootFolder / "content"
    val outputFolder = siteRootFolder / "_site"
    val cacheFolder = siteRootFolder / ".flatmark-cache"
    
    os.remove.all(outputFolder)
    
    val siteConfigYaml = if os.exists(siteConfigFile) then os.read(siteConfigFile) else "name: My Site"
    val siteConfig = siteConfigYaml.as[SiteConfig].toOption.getOrElse {
      throw RuntimeException(s"Invalid site config in file: ${siteConfigFile}. Expected SiteConfig format.")
    }
    
    val fileCache = FileCache(cacheFolder, useCache)

    val codeHighlighter = FlatmarkCodeHighlighter(port, chromeDriverHolder, fileCache)
    val graphvizRenderer = FlatmarkGraphvizRenderer(port, chromeDriverHolder, fileCache)
    val mathRenderer = FlatmarkMathRenderer(port, chromeDriverHolder, fileCache)
    val markdownRenderer = FlatmarkMarkdownRenderer(codeHighlighter, graphvizRenderer, mathRenderer)
    val templateHandler = FlatmarkTemplateHandler(siteRootFolder)

    // TODO custom config
    // skip any file/folder that starts with . or _
    def shouldSkip(file: os.Path) =
      file.segments.exists(s => s.startsWith(".") || s.startsWith("_"))

    
    os.walk(contentFolder, skip = shouldSkip).foreach { file =>
      if file.ext == "md" then {
        renderMarkdownFile(
          siteConfig,
          contentFolder,
          file,
          outputFolder,
          markdownRenderer,
          templateHandler
        )
      } else if os.isFile(file) then {
        os.copy(
          file,
          outputFolder / file.relativeTo(contentFolder),
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
      contentFolder: os.Path,
      file: os.Path,
      outputFolder: os.Path,
      markdownRenderer: FlatmarkMarkdownRenderer,
      templateHandler: FlatmarkTemplateHandler
  ): Unit = {
    logger.fine(s"Markdown file rendering: ${file}")
    val mdContentTemplateRaw = os.read(file)
    val pageConfig = parseConfig(file.baseName, mdContentTemplateRaw)
    val templateConfig = TemplateConfig(siteConfig, pageConfig)
    val mdContent = templateHandler.render(file.relativeTo(contentFolder).segments.mkString("/"), templateContext(templateConfig))
    val mdHtmlContent = markdownRenderer.renderMarkdown(mdContent)
    // render final HTML file
    val htmlContent = templateHandler.render(
      pageConfig.layout,
      templateContext(templateConfig.copy(page = pageConfig.copy(content = mdHtmlContent)))
    )
    os.write.over(
      outputFolder / file.relativeTo(contentFolder).segments.init / s"${file.baseName}.html",
      htmlContent,
      createFolders = true
    )
    logger.fine(s"Markdown file rendered: ${file}")
  }

  private def parseConfig(fileNameBase: String, mdTemplateRaw: String): PageConfig = {
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
      val rawYaml = mdTemplateRaw.linesIterator
        .slice(firstTripleDashIndex + 1, firstTripleDashIndex + 1 + secondTripleDashIndex - firstTripleDashIndex - 1)
        .mkString("\n")
      rawYaml.as[PageConfig].toOption.getOrElse {
        throw RuntimeException(s"Invalid YAML front matter in file: ${fileNameBase}. Expected PageConfig format.")
      }
    } else PageConfig()
  }

  private def templateContext(templateConfig: TemplateConfig) =
    Map(
      "site" -> yamlNodeToObject(YamlCodec[SiteConfig].asNode(templateConfig.site)),
      "page" -> yamlNodeToObject(YamlCodec[PageConfig].asNode(templateConfig.page))
    ).asJava

  private def yamlNodeToObject(node: Node): Object = node match {
    case sn: Node.ScalarNode   => sn.value
    case sn: Node.SequenceNode => sn.nodes.map(yamlNodeToObject).asJava
    case mn: Node.MappingNode =>
      mn.mappings.map { case (key, value) =>
        yamlNodeToObject(key) -> yamlNodeToObject(value)
      }.asJava
  }
}

case class TemplateConfig(site: SiteConfig, page: PageConfig) derives YamlCodec

case class SiteConfig(name: String = "My Site", description: String = "") derives YamlCodec

case class PageConfig(
    layout: String = "default",
    title: String = "Untitled",
    description: String = "",
    content: String = ""
) derives YamlCodec
