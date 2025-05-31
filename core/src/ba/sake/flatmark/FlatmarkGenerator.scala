package ba.sake.flatmark

import java.util.logging.Logger
import scala.util.boundary
import scala.jdk.CollectionConverters.*
import org.virtuslab.yaml.*
import ba.sake.flatmark.selenium.ChromeDriverHolder
import ba.sake.flatmark.markdown.FlatmarkMarkdownRenderer
import ba.sake.flatmark.codehighlight.FlatmarkCodeHighlighter
import ba.sake.flatmark.diagrams.FlatmarkGraphvizRenderer
import ba.sake.flatmark.math.FlatmarkMathRenderer

class FlatmarkGenerator(port: Int, chromeDriverHolder: ChromeDriverHolder) {
  private val logger = Logger.getLogger(getClass.getName)

  def generate(siteRootFolder: os.Path, useCache: Boolean): Unit = {
    logger.info(s"Generating site in folder: ${siteRootFolder}")

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
      if file.ext == "md" || file.ext == "html" then {
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
    val mdContent =
      templateHandler.render(file.relativeTo(contentFolder).segments.mkString("/"), templateContext(templateConfig))
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
