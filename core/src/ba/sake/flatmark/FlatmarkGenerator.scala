package ba.sake.flatmark

import java.util.Locale
import org.slf4j.LoggerFactory
import scala.collection.mutable
import org.virtuslab.yaml.*
import ba.sake.flatmark.selenium.WebDriverHolder
import ba.sake.flatmark.markdown.FlatmarkMarkdownRenderer
import ba.sake.flatmark.codehighlight.FlatmarkCodeHighlighter
import ba.sake.flatmark.diagrams.FlatmarkGraphvizRenderer
import ba.sake.flatmark.diagrams.FlatmarkMermaidRenderer
import ba.sake.flatmark.math.FlatmarkMathRenderer
import ba.sake.flatmark.templates.FlatmarkTemplateHandler

class FlatmarkGenerator(ssrServerPort: Int, webDriverHolder: WebDriverHolder) {
  private val logger = LoggerFactory.getLogger(getClass.getName)

  private val Iso2LanguageCodes = Set(Locale.getISOLanguages*)

  def generate(siteRootFolder: os.Path, useCache: Boolean): Unit = {
    logger.info(s"Generating site in '${siteRootFolder}'")
    if !os.exists(siteRootFolder) then throw RuntimeException(s"Site root folder does not exist: ${siteRootFolder}")
    if !os.isDir(siteRootFolder) then throw RuntimeException(s"Site root is not a folder: ${siteRootFolder}")

    val customClassloader = new java.net.URLClassLoader(
      Array((siteRootFolder / "_i18n").toIO.toURI.toURL),
      Thread.currentThread.getContextClassLoader
    )

    val outputFolder = siteRootFolder / "_site"
    
    // TODO remove files that are not part of this build
    /*os.list(outputFolder).foreach { file =>
      os.remove.all(file, ignoreErrors = true)
    }*/

    val siteConfigFile = siteRootFolder / "_config.yaml"
    val siteConfigYaml = if os.exists(siteConfigFile) then os.read(siteConfigFile) else "name: My Site"
    val siteConfig: SiteConfig = siteConfigYaml.as[SiteConfig].toOption.getOrElse {
      throw RuntimeException(s"Invalid site config in file: ${siteConfigFile}. Expected SiteConfig format.")
    }
    logger.debug(s"Site configuration: ${siteConfig}")

    // collect relevant content
    val contentFolder = siteRootFolder / "content"
    def shouldSkip(file: os.Path) =
      file.segments.exists(s => s.startsWith(".") || s.startsWith("_"))
    val processFiles = mutable.ArrayBuffer.empty[ProcessFile]
    val translationsLangCodes = mutable.ArrayBuffer.empty[String]
    os.walk(contentFolder, skip = shouldSkip).flatMap { file =>
      Option.when(os.isFile(file)) {
        val processFile =
          if file.ext == "md" || file.ext == "html"
          then ProcessFile.TemplatedFile(file)
          else ProcessFile.StaticFile(file)
        val rootRelPath = file.relativeTo(contentFolder)
        val firstSegment = rootRelPath.segments.head
        if Iso2LanguageCodes(firstSegment) then translationsLangCodes += firstSegment
        processFiles += processFile
      }
    }

    val cacheFolder = siteRootFolder / ".flatmark-cache"
    val fileCache = FileCache(cacheFolder, useCache)
    val codeHighlighter = FlatmarkCodeHighlighter(ssrServerPort, webDriverHolder, fileCache)
    val graphvizRenderer = FlatmarkGraphvizRenderer(ssrServerPort, webDriverHolder, fileCache)
    val mermaidRenderer = FlatmarkMermaidRenderer(ssrServerPort, webDriverHolder, fileCache)
    val mathRenderer = FlatmarkMathRenderer(ssrServerPort, webDriverHolder, fileCache)
    val markdownRenderer = FlatmarkMarkdownRenderer(codeHighlighter, graphvizRenderer, mermaidRenderer, mathRenderer)
    val templateHandler = FlatmarkTemplateHandler(customClassloader, siteRootFolder)

    val templatedIndexFiles = mutable.ArrayBuffer.empty[ProcessFile.TemplatedFile]
    val templatedContentFiles = mutable.ArrayBuffer.empty[ProcessFile.TemplatedFile]
    processFiles.collect { case tf: ProcessFile.TemplatedFile =>
      if tf.file.baseName == "index" then templatedIndexFiles += tf
      else templatedContentFiles += tf
    }

    // TODO pokupit sve pathove za content files
    // i proslijedit pebbleu za custom translation_url

    // generate content first to get their snippets for index pages
    val allUsedLanguages =
      translationsLangCodes.prepend(siteConfig.lang.toLanguageTag).distinct.sorted.map(Locale.forLanguageTag).toSeq
    val contentResults = templatedContentFiles.flatMap { tf =>
      renderTemplatedFile(
        siteConfig,
        allUsedLanguages,
        contentFolder = contentFolder,
        file = tf.file,
        outputFolder = outputFolder,
        markdownRenderer,
        templateHandler,
        paginateItems = None
      )
    }.toSeq
    // generate index files with pagination and all
    val contentByLangAndCategory: mutable.Map[(String, String), Seq[PageContext]] = (for
      lang <- allUsedLanguages.map(_.toLanguageTag)
      cat <- siteConfig.categories.keys
    yield (lang, cat) -> Seq.empty[PageContext]).to(mutable.Map)
    contentResults.foreach { cr =>
      val segments = cr.pageContext.rootRelPath.segments
      val firstSegment = segments.head
      val key =
        if translationsLangCodes.contains(firstSegment)
        then (firstSegment, segments(1)) // (lang, category)
        else (siteConfig.lang.toLanguageTag, firstSegment) // (default lang, category)
      contentByLangAndCategory.get(key) match {
        case Some(contentPages) =>
          contentByLangAndCategory.update(key, contentPages.appended(cr.pageContext))
        case None =>
          // noop for a top level page without category: about.md etc
      }
    }
    templatedIndexFiles.foreach { tf =>
      val segments = tf.file.relativeTo(contentFolder).segments
      val firstSegment = segments.head
      val key =
        if translationsLangCodes.contains(firstSegment)
        then (firstSegment, segments(1)) // (lang, category)
        else (siteConfig.lang.toLanguageTag, firstSegment) // (default lang, category)
      val categoryItems = contentByLangAndCategory.getOrElse(
        key,
        contentByLangAndCategory.filter(_._1._1 == key._1).values.flatten.toSeq // by default use all content pages
      )
      renderTemplatedFile(
        siteConfig,
        allUsedLanguages,
        contentFolder = contentFolder,
        file = tf.file,
        outputFolder = outputFolder,
        markdownRenderer,
        templateHandler,
        paginateItems = Some(categoryItems)
      )
    }
    // copy static files (e.g. images, css..)
    // TODO separate folder, noice
    processFiles.collect { case pf: ProcessFile.StaticFile =>
      os.copy(
        pf.file,
        outputFolder / pf.file.relativeTo(contentFolder),
        replaceExisting = true,
        createFolders = true,
        mergeFolders = true,
        followLinks = false
      )
    }
    logger.info(s"Site generated successfully")
  }

  private def renderTemplatedFile(
      siteConfig: SiteConfig,
      languages: Seq[Locale],
      contentFolder: os.Path,
      file: os.Path,
      outputFolder: os.Path,
      markdownRenderer: FlatmarkMarkdownRenderer,
      templateHandler: FlatmarkTemplateHandler,
      paginateItems: Option[Seq[PageContext]]
  ): Seq[RenderResult] = {
    logger.debug(s"Rendering templated file: ${file}")
    val mdContentTemplateRaw = os.read(file)
    val pageConfig = parseConfig(file.baseName, mdContentTemplateRaw)
    val templateConfig = TemplateConfig(siteConfig, pageConfig)
    val fileRelPath = file.relativeTo(contentFolder)
    val locale =
      if fileRelPath.segments.length > 1 && Iso2LanguageCodes(fileRelPath.segments.head) then
        Locale.forLanguageTag(fileRelPath.segments.head)
      else siteConfig.lang
    Locale.setDefault(Locale.Category.DISPLAY, locale) // set locale for rendering language names in current language

    paginateItems match {
      case Some(allItems) =>
        val pageSize = 10
        val paginatedItems = allItems.grouped(pageSize).toSeq
        for (paginatedItemsGroup, i) <- paginatedItems.zipWithIndex yield {
          def rootRelPath(pageNum: Int): os.RelPath = {
            val pageNumSuffix = if pageNum == 1 then "" else s"-${pageNum}"
            os.RelPath(
              if fileRelPath.segments.length == 1 then s"${file.baseName}${pageNumSuffix}.html"
              else s"${fileRelPath.segments.init.mkString("/")}/${file.baseName}${pageNumSuffix}.html"
            )
          }
          val contentContext = templateContext(
            languages,
            locale,
            templateConfig,
            rootRelPath,
            paginatedItemsGroup,
            currentPage = i + 1,
            pageSize = pageSize,
            totalItems = allItems.length
          )
          renderTemplatedFileSingle(
            contentFolder,
            file,
            contentContext,
            outputFolder,
            markdownRenderer,
            templateHandler,
            locale
          )
        }
      case None =>
        val rootRelPath = os.RelPath(
          if fileRelPath.segments.length == 1 then s"${file.baseName}.html"
          else s"${fileRelPath.segments.init.mkString("/")}/${file.baseName}.html"
        )
        val contentContext = templateContext(
          languages,
          locale,
          templateConfig,
          _ => rootRelPath,
          Seq.empty,
          currentPage = 0,
          pageSize = 0,
          totalItems = 0
        )
        Seq(
          renderTemplatedFileSingle(
            contentFolder,
            file,
            contentContext,
            outputFolder,
            markdownRenderer,
            templateHandler,
            locale
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
      templateHandler: FlatmarkTemplateHandler,
      locale: Locale
  ): RenderResult = {
    val fileRelPath = file.relativeTo(contentFolder)
    val content = templateHandler.render(fileRelPath.toString, contentContext.toPebbleContext, locale)
    val contentHtml = if file.ext == "md" then markdownRenderer.renderMarkdown(content) else content
    // render final HTML file
    val layoutContext = contentContext.copy(page = contentContext.page.copy(content = contentHtml))
    val finalHtml = templateHandler.render(contentContext.page.layout, layoutContext.toPebbleContext, locale)
    os.write.over(
      outputFolder / contentContext.page.rootRelPath,
      finalHtml,
      createFolders = true
    )
    logger.debug(s"Rendered templated file: ${file}")
    RenderResult(layoutContext.page)
  }

  private def templateContext(
      languages: Seq[Locale],
      lang: Locale,
      templateConfig: TemplateConfig,
      rootRelPath: Int => os.RelPath,
      items: Seq[PageContext],
      currentPage: Int,
      pageSize: Int,
      totalItems: Int
  ): TemplateContext = {
    val (langContexts, langContext) = locally {
      val originalLocale = Locale.getDefault(Locale.Category.DISPLAY)
      val res1 = languages.map { l =>
        // set locale for rendering language names in respective language
        Locale.setDefault(Locale.Category.DISPLAY, l)
        val url = if templateConfig.site.lang == l then "/" else s"/${l.toLanguageTag}"
        LanguageContext(l.toLanguageTag, l.getDisplayLanguage, url)
      }
      val res2 = LanguageContext(
        lang.toLanguageTag,
        lang.getDisplayLanguage,
        if templateConfig.site.lang == lang then "/" else s"/${lang.toLanguageTag}"
      )
      Locale.setDefault(Locale.Category.DISPLAY, originalLocale) // restore original locale
      (res1, res2)
    }

    TemplateContext(
      SiteContext(
        name = templateConfig.site.name,
        description = templateConfig.site.description,
        langs = langContexts,
        categories = templateConfig.site.categories.map { case (key, value) =>
          key -> CategoryContext(value.label, value.description)
        },
        tags = templateConfig.site.tags.map { case (key, value) => key -> TagContext(value.label, value.description) }
      ),
      PageContext(
        layout = templateConfig.page.layout,
        title = templateConfig.page.title,
        description = templateConfig.page.description,
        content = "",
        lang = langContext,
        publishDate = templateConfig.page.publishDate.map(_.atZone(templateConfig.site.timezone.toZoneId)),
        rootRelPath = rootRelPath(currentPage)
      ),
      Option.when(items.nonEmpty)(
        PaginatorContext(
          currentPage = currentPage,
          items = items,
          totalItems = totalItems,
          pageSize = pageSize,
          rootRelPath = rootRelPath
        )
      )
    )
  }

}

enum ProcessFile:
  case TemplatedFile(file: os.Path)
  case StaticFile(file: os.Path)

case class RenderResult(
    pageContext: PageContext
)
