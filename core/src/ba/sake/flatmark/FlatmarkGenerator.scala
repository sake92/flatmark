package ba.sake.flatmark

import java.util.logging.Logger
import scala.collection.mutable
import org.virtuslab.yaml.*
import ba.sake.flatmark.selenium.ChromeDriverHolder
import ba.sake.flatmark.markdown.FlatmarkMarkdownRenderer
import ba.sake.flatmark.codehighlight.FlatmarkCodeHighlighter
import ba.sake.flatmark.diagrams.FlatmarkGraphvizRenderer
import ba.sake.flatmark.math.FlatmarkMathRenderer

class FlatmarkGenerator(port: Int, chromeDriverHolder: ChromeDriverHolder) {
  private val logger = Logger.getLogger(getClass.getName)

  def generate(siteRootFolder: os.Path, useCache: Boolean): Unit = {
    if !os.exists(siteRootFolder) then
      throw RuntimeException(s"Site root folder does not exist: ${siteRootFolder}")
    if !os.isDir(siteRootFolder) then
        throw RuntimeException(s"Site root folder is not a folder: ${siteRootFolder}")
    logger.info(s"Generating site in: ${siteRootFolder}")

    val siteConfigFile = siteRootFolder / "_config.yaml"
    val contentFolder = siteRootFolder / "content"
    val outputFolder = siteRootFolder / "_site"
    val cacheFolder = siteRootFolder / ".flatmark-cache"

    os.remove.all(outputFolder, ignoreErrors = true)

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

    val templatedIndexFiles = mutable.ArrayBuffer.empty[ProcessFile.TemplatedFile]
    val templatedNonIndexFiles = mutable.ArrayBuffer.empty[ProcessFile.TemplatedFile]
    val templatedPostFiles = mutable.ArrayBuffer.empty[ProcessFile.TemplatedFile]
    processFiles.collect { case tf: ProcessFile.TemplatedFile =>
      val fileRelPath = tf.file.relativeTo(contentFolder)
      if tf.file.baseName == "index" then templatedIndexFiles += tf
      else if fileRelPath.segments.length > 1 && fileRelPath.segments.head == "posts" then templatedPostFiles += tf
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
        unPaginatedPosts = Seq.empty
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
        unPaginatedPosts = Seq.empty
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
        unPaginatedPosts = postResults.flatten.map(_.pageContext)
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
      unPaginatedPosts: Seq[PageContext]
  ): Seq[RenderResult] = {
    logger.fine(s"Rendering templated file: ${file}")
    val mdContentTemplateRaw = os.read(file)
    val pageConfig = parseConfig(file.baseName, mdContentTemplateRaw)
    val templateConfig = TemplateConfig(siteConfig, pageConfig)
    val fileRelPath = file.relativeTo(contentFolder)

    pageConfig.paginate match {
      case Some(paginateConfig) =>
        paginateConfig.data match {
          case "site.posts" =>
            val posts = unPaginatedPosts
            val paginatedPosts = posts.grouped(paginateConfig.size).toSeq
            for (paginatedPostsGroup, i) <- paginatedPosts.zipWithIndex yield {
              def outputFileRelPath(pageNum: Int) = {
                val pageNumSuffix = if pageNum == 1 then "" else s"-${pageNum}"
                s"/${fileRelPath.segments.init.mkString("/")}/${file.baseName}${pageNumSuffix}.html"
              }
              val contentContext = templateContext(
                templateConfig,
                outputFileRelPath,
                paginatedPostsGroup,
                currentPage = i + 1,
                pageSize = paginateConfig.size,
                totalItems = posts.length
              )
              renderTemplatedFileSingle(
                contentFolder,
                file,
                contentContext,
                outputFolder,
                markdownRenderer,
                templateHandler
              )
            }
          case _ =>
            throw RuntimeException(s"Unsupported pagination data: ${paginateConfig.data} in file: ${file}")
        }
      case None =>
        val outputFileRelPath = s"/${fileRelPath.segments.init.mkString("/")}/${file.baseName}.html"
        val contentContext = templateContext(
          templateConfig,
          _ => outputFileRelPath,
          unPaginatedPosts,
          currentPage = 1,
          pageSize = 10,
          totalItems = unPaginatedPosts.length
        )
        Seq(
          renderTemplatedFileSingle(
            contentFolder,
            file,
            contentContext,
            outputFolder,
            markdownRenderer,
            templateHandler
          )
        )
    }
  }
  
  private def renderTemplatedFileSingle(
      contentFolder: os.Path,
      file: os.Path,
      contentContext: TemplateContext,
      outputFolder: os.Path,
      markdownRenderer: FlatmarkMarkdownRenderer,
      templateHandler: FlatmarkTemplateHandler
  ): RenderResult = {
    val fileRelPath = file.relativeTo(contentFolder)
    val content = templateHandler.render(fileRelPath.toString, contentContext.toPebbleContext)
    val contentHtml = if file.ext == "md" then markdownRenderer.renderMarkdown(content) else content
    // render final HTML file
    val layoutContext = contentContext.copy(page = contentContext.page.copy(content = contentHtml))
    val finalHtml = templateHandler.render(contentContext.page.layout, layoutContext.toPebbleContext)
    os.write.over(
      outputFolder / os.SubPath(contentContext.page.rootRelPath.dropWhile(_ == '/')),
      finalHtml,
      createFolders = true
    )
    logger.fine(s"Rendered templated file: ${file}")
    RenderResult(layoutContext.page)
  }

  private def templateContext(
      templateConfig: TemplateConfig,
      outputFileRelPath: Int => String,
      posts: Seq[PageContext],
      currentPage: Int,
      pageSize: Int,
      totalItems: Int
  ): TemplateContext =
    TemplateContext(
      SiteContext(
        name = templateConfig.site.name,
        description = templateConfig.site.description,
        posts = posts
      ),
      PageContext(
        layout = templateConfig.page.layout,
        title = templateConfig.page.title,
        description = templateConfig.page.description,
        content = templateConfig.page.content,
        rootRelPath = outputFileRelPath(currentPage)
      ),
      Option.when(posts.nonEmpty)(
        PaginatorContext(
          currentPage = currentPage,
          items = posts,
          totalItems = totalItems,
          pageSize = pageSize,
          outputFileRelPath = outputFileRelPath
        )
      )
    )

}

enum ProcessFile:
  case TemplatedFile(file: os.Path)
  case PlainFile(file: os.Path)

case class RenderResult(
    pageContext: PageContext
)
