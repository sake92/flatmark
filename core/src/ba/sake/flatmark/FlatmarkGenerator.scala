package ba.sake.flatmark

import java.util.Locale
import org.slf4j.LoggerFactory
import scala.jdk.CollectionConverters.*
import scala.collection.mutable
import org.virtuslab.yaml.*
import io.undertow.util.QueryParameterUtils
import ba.sake.flatmark.selenium.WebDriverHolder
import ba.sake.flatmark.markdown.FlatmarkMarkdownRenderer
import ba.sake.flatmark.codehighlight.FlatmarkCodeHighlighter
import ba.sake.flatmark.diagrams.FlatmarkGraphvizRenderer
import ba.sake.flatmark.diagrams.FlatmarkMermaidRenderer
import ba.sake.flatmark.math.FlatmarkMathRenderer
import ba.sake.flatmark.templates.FlatmarkTemplateHandler
import ba.sake.querson.*

class FlatmarkGenerator(ssrServerUrl: String, webDriverHolder: WebDriverHolder) {
  private val logger = LoggerFactory.getLogger(getClass.getName)

  private val Iso2LanguageCodes = Set(Locale.getISOLanguages*)

  def generate(siteRootFolder: os.Path, useCache: Boolean): Unit = {
    logger.info(s"Generating site in '${siteRootFolder}'")
    if !os.exists(siteRootFolder) then throw FlatmarkException(s"Site root folder does not exist: ${siteRootFolder}")
    if !os.isDir(siteRootFolder) then throw FlatmarkException(s"Site root is not a folder: ${siteRootFolder}")

    val outputFolder = siteRootFolder / "_site"

    // TODO remove files that are not part of this build
    /*os.list(outputFolder).foreach { file =>
      os.remove.all(file, ignoreErrors = true)
    }*/

    val siteConfigFile = siteRootFolder / "_config.yaml"
    val siteConfigYaml = if os.exists(siteConfigFile) then os.read(siteConfigFile) else "name: My Site"
    val siteConfig: SiteConfig = siteConfigYaml.as[SiteConfig].toOption.getOrElse {
      throw FlatmarkException(s"Invalid site config in file: ${siteConfigFile}. Expected SiteConfig format.")
    }
    logger.debug(s"Site configuration: ${siteConfig}")

    // collect relevant content
    val contentFolder = siteRootFolder / "content"
    def shouldSkip(file: os.Path) =
      file.segments.exists(s => s.startsWith(".") || s.startsWith("_"))
    val processFiles = mutable.ArrayBuffer.empty[os.Path]
    val translationsLangCodes = mutable.ArrayBuffer.empty[String]
    if os.exists(contentFolder) then
      if !os.isDir(contentFolder) then
        throw FlatmarkException(s"The 'content/' folder is not a folder: ${contentFolder}")
      if os.list(contentFolder).isEmpty then logger.warn(s"The 'content/' folder is empty, no content to process.")
      os.walk(contentFolder, skip = shouldSkip).flatMap { file =>
        Option.when(os.isFile(file) && (file.ext == "md" || file.ext == "html")) {
          val rootRelPath = file.relativeTo(contentFolder)
          val firstSegment = rootRelPath.segments.head
          if Iso2LanguageCodes(firstSegment) then translationsLangCodes += firstSegment
          processFiles += file
        }
      }
    else logger.warn(s"The 'content/' folder does not exist, skipping content processing.")

    val cacheFolder = siteRootFolder / ".flatmark-cache"
    val themesFolder = cacheFolder / "themes"
    val themeFolder = downloadThemeRepo(siteConfig.theme, themesFolder, useCache)
    val fileCache = FileCache(cacheFolder, useCache)
    val codeHighlighter = FlatmarkCodeHighlighter(ssrServerUrl, webDriverHolder, fileCache)
    val graphvizRenderer = FlatmarkGraphvizRenderer(ssrServerUrl, webDriverHolder, fileCache)
    val mermaidRenderer = FlatmarkMermaidRenderer(ssrServerUrl, webDriverHolder, fileCache)
    val mathRenderer = FlatmarkMathRenderer(ssrServerUrl, webDriverHolder, fileCache)
    val markdownRenderer = FlatmarkMarkdownRenderer(codeHighlighter, graphvizRenderer, mermaidRenderer, mathRenderer)
    val customClassloader = new java.net.URLClassLoader(
      Array(siteRootFolder / "_i18n", themeFolder / "_i18n").map(_.toIO.toURI.toURL),
      Thread.currentThread.getContextClassLoader
    )
    val templateHandler = FlatmarkTemplateHandler(customClassloader, siteRootFolder, themeFolder)

    // copy theme static files
    val themeStaticFolder = themeFolder / "static"
    if os.exists(themeStaticFolder) then
      os.walk(themeStaticFolder).foreach { file =>
        if os.isFile(file) then
          os.copy(
            file,
            outputFolder / file.relativeTo(themeFolder),
            replaceExisting = true,
            createFolders = true,
            mergeFolders = true,
            followLinks = false
          )
      }

    val templatedIndexFiles = mutable.ArrayBuffer.empty[os.Path]
    val templatedContentFiles = mutable.ArrayBuffer.empty[os.Path]
    processFiles.foreach { file =>
      if file.baseName == "index" then templatedIndexFiles += file
      else templatedContentFiles += file
    }

    // TODO pokupit sve pathove za content files
    // i proslijedit pebbleu za custom translation_url

    // generate content first to get their snippets for index pages
    val allUsedLanguages =
      translationsLangCodes.prepend(siteConfig.lang.toLanguageTag).distinct.sorted.map(Locale.forLanguageTag).toSeq
    val contentResults = templatedContentFiles.flatMap { file =>
      renderTemplatedFile(
        siteConfig,
        allUsedLanguages,
        contentFolder = contentFolder,
        file = file,
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
    templatedIndexFiles.foreach { file =>
      val segments = file.relativeTo(contentFolder).segments
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
        file = file,
        outputFolder = outputFolder,
        markdownRenderer,
        templateHandler,
        paginateItems = Some(categoryItems)
      )
    }
    // copy static files (e.g. images, css..)
    val staticFolder = siteRootFolder / "static"
    if os.exists(staticFolder) then
      os.walk(staticFolder).foreach { file =>
        if os.isFile(file) then
          os.copy(
            file,
            outputFolder / file.relativeTo(staticFolder),
            replaceExisting = true,
            createFolders = true,
            mergeFolders = true,
            followLinks = false
          )
      }
    logger.info("Site generated successfully")
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
            defaultLayout = "index",
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
          defaultLayout = "page",
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
      defaultLayout: String,
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
        layout = templateConfig.page.layout.getOrElse(defaultLayout),
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

  /** returns the folder that contains theme */
  private def downloadThemeRepo(url: String, themesFolder: os.Path, useCache: Boolean): os.Path = {
    val parsedUri = java.net.URI.create(url)
    val qp = QueryParameterUtils
      .parseQueryString(parsedUri.getQuery, "utf-8")
      .asScala
      .map((k, v) => k -> v.asScala.toSeq)
      .toMap
      .parseQueryStringMap[ThemeUrlQP]
    val themeHash =
      s"${parsedUri.getScheme}-${parsedUri.getHost}${parsedUri.getPath}-${HashUtils.generate(url)}".replace("/", "-")
    val themeRepoFolder = themesFolder / themeHash
    if os.exists(themeRepoFolder) && useCache then {
      logger.debug("Theme is already downloaded. Skipping download.")
    } else {
      val httpCloneUrl = s"${parsedUri.getScheme}://${parsedUri.getHost}${parsedUri.getPath}.git"
      logger.info(s"Downloading theme from ${httpCloneUrl}")
      // TODO fallback to ssh and api
      os.makeDir.all(themesFolder)
      os.call(
        ("git", "clone", "--depth", "1", "--branch", qp.branch, httpCloneUrl, themeHash),
        cwd = themesFolder
      )
      logger.info(s"Downloaded theme from: ${url}")
    }
    themeRepoFolder / os.RelPath(qp.folder)
  }

}

case class RenderResult(
    pageContext: PageContext
)

case class ThemeUrlQP(
    branch: String = "main",
    folder: String = "."
) derives QueryStringRW

class FlatmarkException(message: String, cause: Throwable = null) extends RuntimeException(message, cause)
