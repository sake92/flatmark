package ba.sake.flatmark

import java.util.logging.Logger
import scala.collection.mutable
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

    val processFiles = os.walk(contentFolder, skip = shouldSkip).flatMap { file =>
      Option.when(os.isFile(file)) {
        if file.ext == "md" || file.ext == "html"
        then ProcessFile.TemplatedFile(file)
        else ProcessFile.PlainFile(file)
      }
    }

    // TODO collect posts and build proper context(s)
    val templatedIndexFiles = mutable.ArrayBuffer.empty[ProcessFile.TemplatedFile]
    val templatedNonIndexFiles = mutable.ArrayBuffer.empty[ProcessFile.TemplatedFile]
    val templatedPostFiles = mutable.ArrayBuffer.empty[ProcessFile.TemplatedFile]
    processFiles.collect { case tf: ProcessFile.TemplatedFile =>
      val fileRelPath = tf.file.relativeTo(contentFolder)
      if fileRelPath.segments.length > 1 && fileRelPath.segments.head == "posts" then templatedPostFiles += tf
      else if tf.file.baseName == "index" then templatedIndexFiles += tf
      else templatedNonIndexFiles += tf
    }
    templatedNonIndexFiles.foreach { tf =>
      renderTemplatedFile(
        siteConfig,
        contentFolder = contentFolder,
        file = tf.file,
        outputFolder = outputFolder,
        markdownRenderer,
        templateHandler,
        posts = Seq.empty
      )
    }
    // need to generate posts first to get their snippets for index page
    val postResults = templatedPostFiles.map { tf =>
      renderTemplatedFile(
        siteConfig,
        contentFolder = contentFolder,
        file = tf.file,
        outputFolder = outputFolder,
        markdownRenderer,
        templateHandler,
        posts = Seq.empty
      )
    }.toSeq
    templatedIndexFiles.foreach { tf =>
      renderTemplatedFile(
        siteConfig,
        contentFolder = contentFolder,
        file = tf.file,
        outputFolder = outputFolder,
        markdownRenderer,
        templateHandler,
        posts = postResults.map(_.pageContext)
      )
    }
    // copy plain files (e.g. images, css, static assets)
    val plainFiles = processFiles.collect { case pf: ProcessFile.PlainFile => pf }
    plainFiles.foreach { pf =>
      os.copy(
        pf.file,
        outputFolder / pf.file.relativeTo(contentFolder),
        replaceExisting = true,
        createFolders = true,
        mergeFolders = true,
        followLinks = false
      )
    }
  }

  private def renderTemplatedFile(
      siteConfig: SiteConfig,
      contentFolder: os.Path,
      file: os.Path,
      outputFolder: os.Path,
      markdownRenderer: FlatmarkMarkdownRenderer,
      templateHandler: FlatmarkTemplateHandler,
      posts: Seq[PageContext]
  ): RenderResult = {
    logger.fine(s"Rendering templated file: ${file}")
    val mdContentTemplateRaw = os.read(file)
    val pageConfig = parseConfig(file.baseName, mdContentTemplateRaw)
    val templateConfig = TemplateConfig(siteConfig, pageConfig)
    val fileRelPath = file.relativeTo(contentFolder)
    val outputFileRelPath = s"${fileRelPath.segments.init.mkString("/")}/${file.baseName}.html"
    val contentContext = templateContext(templateConfig, outputFileRelPath, posts)
    val content = templateHandler.render(
      fileRelPath.toString,
      contentContext.toPebbleContext
    )
    val mdHtmlContent = markdownRenderer.renderMarkdown(content)
    // render final HTML file
    val layoutContext =
      templateContext(templateConfig.copy(page = pageConfig.copy(content = mdHtmlContent)), outputFileRelPath, posts)
    val htmlContent = templateHandler.render(
      pageConfig.layout,
      layoutContext.toPebbleContext
    )
    os.write.over(
      outputFolder / fileRelPath.segments.init / s"${file.baseName}.html",
      htmlContent,
      createFolders = true
    )
    logger.fine(s"Rendered templated file: ${file}")
    RenderResult(layoutContext.page)
  }

  private def templateContext(
      templateConfig: TemplateConfig,
      outputFileRelPath: String,
      posts: Seq[PageContext]
  ): TemplateContext =
    TemplateContext(
      SiteContext(
        name = templateConfig.site.name,
        description = templateConfig.site.description,
        posts = posts
      ),
      PageContext(
        title = templateConfig.page.title,
        description = templateConfig.page.description,
        content = templateConfig.page.content,
        url = outputFileRelPath
      )
    )

}

enum ProcessFile:
  case TemplatedFile(file: os.Path)
  case PlainFile(file: os.Path)

case class RenderResult(
    pageContext: PageContext
)
