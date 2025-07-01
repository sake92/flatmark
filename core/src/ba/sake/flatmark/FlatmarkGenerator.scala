package ba.sake.flatmark

import java.util.Locale
import scala.jdk.CollectionConverters.*
import scala.collection.mutable
import scala.util.control.NonFatal
import org.jsoup.Jsoup
import org.slf4j.LoggerFactory
import org.virtuslab.yaml.*
import ba.sake.flatmark.selenium.WebDriverHolder
import ba.sake.flatmark.markdown.FlatmarkMarkdownRenderer
import ba.sake.flatmark.codehighlight.FlatmarkCodeHighlighter
import ba.sake.flatmark.diagrams.FlatmarkGraphvizRenderer
import ba.sake.flatmark.diagrams.FlatmarkMermaidRenderer
import ba.sake.flatmark.math.FlatmarkMathRenderer
import ba.sake.flatmark.search.SearchEntry
import ba.sake.flatmark.templates.FlatmarkTemplateHandler
import ba.sake.tupson.{JsonRW, toJson}
import YamlInstances.given

import java.time.{Instant, ZonedDateTime}
import scala.util.Properties

class FlatmarkGenerator(ssrServerUrl: String, webDriverHolder: WebDriverHolder, updateTheme: Boolean) {
  private val logger = LoggerFactory.getLogger(getClass.getName)

  private val Iso2LanguageCodes = Set(Locale.getISOLanguages*)

  def generate(siteRootFolder: os.Path, useCache: Boolean): Boolean = try {
    logger.info(s"Generating site in '${siteRootFolder}'")
    if !os.exists(siteRootFolder) then throw FlatmarkException(s"Site root folder does not exist: ${siteRootFolder}")
    if !os.isDir(siteRootFolder) then throw FlatmarkException(s"Site root is not a folder: ${siteRootFolder}")

    val outputFolder = siteRootFolder / "_site"

    // TODO remove files that are not part of this build
    /*os.list(outputFolder).foreach { file =>
      os.remove.all(file, ignoreErrors = true)
    }*/

    val siteConfigFile = siteRootFolder / "_config.yaml"
    val defaultSiteConfig = "name: My Site"
    val siteConfigYaml = if os.exists(siteConfigFile) then {
      val siteConfigYamlStr = os.read(siteConfigFile)
      // virtuslab yaml library does not support empty strings, so we use a default config
      if siteConfigYamlStr.isBlank then defaultSiteConfig else siteConfigYamlStr
    } else defaultSiteConfig
    val siteConfig: SiteConfig = siteConfigYaml.as[SiteConfig] match {
      case Right(config) =>
        val baseUrl = config.base_url
          .orElse {
            System.getenv().asScala.get("FLATMARK_BASE_URL").filterNot(_.isBlank)
          }
          .map(_.stripSuffix("/"))
        config.copy(base_url = baseUrl)
      case Left(error) =>
        throw FlatmarkException(s"Invalid site config in file: ${siteConfigFile}. Expected SiteConfig format.", error)
    }
    logger.debug(s"Site configuration: ${siteConfig}")

    // read _data files
    val dataFolder = siteRootFolder / "_data"
    val dataYamls = if os.exists(dataFolder) then {
      val dataYamlFiles = os.list(dataFolder).filter(_.ext == "yaml").filter(os.isFile(_))
      dataYamlFiles.map { file =>
        val yamlContent = os.read(file)
        if yamlContent.isBlank then Map.empty
        val yaml = yamlContent.as[Node].getOrElse {
          throw FlatmarkException(s"Invalid YAML format in file: ${file}. Expected Map[String, Node].")
        }
        file.baseName -> yaml
      }.toMap
    } else Map.empty

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
        Option.when(os.isFile(file) && (file.ext == "md" || file.ext.endsWith("html"))) {
          val rootRelPath = file.relativeTo(contentFolder)
          val firstSegment = rootRelPath.segments.head
          if Iso2LanguageCodes(firstSegment) then translationsLangCodes += firstSegment
          processFiles += file
        }
      }
    else logger.warn(s"The 'content/' folder does not exist, skipping content processing.")

    val cacheFolder = siteRootFolder / ".flatmark-cache"
    val themesCacheFolder = cacheFolder / "themes"
    val localThemesFolder = siteRootFolder / "_themes"
    val themeFolder = Option.when(siteConfig.theme.enabled)(
      ThemeResolver.resolve(siteConfig.theme.source, localThemesFolder, themesCacheFolder, updateTheme)
    )
    val fileCache = FileCache(cacheFolder, useCache)
    val codeHighlighter = FlatmarkCodeHighlighter(ssrServerUrl, webDriverHolder, fileCache)
    val graphvizRenderer = FlatmarkGraphvizRenderer(ssrServerUrl, webDriverHolder, fileCache)
    val mermaidRenderer = FlatmarkMermaidRenderer(ssrServerUrl, webDriverHolder, fileCache)
    val mathRenderer = FlatmarkMathRenderer(ssrServerUrl, webDriverHolder, fileCache)
    val markdownRenderer = FlatmarkMarkdownRenderer(codeHighlighter, graphvizRenderer, mermaidRenderer, mathRenderer)
    val customClassloader = new java.net.URLClassLoader(
      (Array(siteRootFolder / "_i18n") ++ themeFolder.map(_ / "_i18n").toArray).map(_.toIO.toURI.toURL),
      Thread.currentThread.getContextClassLoader
    )
    val templateHandler = FlatmarkTemplateHandler(customClassloader, siteRootFolder, themeFolder, markdownRenderer)

    // copy theme static files
    themeFolder.foreach { folder =>
      val themeStaticFolder = folder / "static"
      if os.exists(themeStaticFolder) then {
        os.walk(themeStaticFolder).foreach { file =>
          if os.isFile(file) then
            os.copy(
              file,
              outputFolder / file.relativeTo(themeStaticFolder),
              replaceExisting = true,
              createFolders = true,
              mergeFolders = true,
              followLinks = false
            )
        }
      }
    }

    // run sass
    val sassFolder = siteRootFolder / "_sass"
    if os.exists(sassFolder) then {
      logger.debug("Compiling Sass files to _site/styles...")
      val sassExe = if Properties.isWin then "sass.bat" else "sass"
      var stderr = ""
      val res = os.call(
        (sassExe, s"${sassFolder}:${outputFolder}/styles"),
        cwd = siteRootFolder,
        stdout = os.ProcessOutput.Readlines(_ => ()),
        stderr = os.ProcessOutput.Readlines(err => stderr += err)
      )
      if res.exitCode != 0 then {
        logger.error(s"Sass compilation failed with exit code ${res.exitCode}:\n${stderr}")
      }
    }

    val templatedContentFiles = mutable.ArrayBuffer.empty[os.Path]
    val templatedIndexFiles = mutable.ArrayBuffer.empty[os.Path]
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
      val contentTemplateRaw = os.read(file)
      val pageConfig = parseConfig(file, contentTemplateRaw)
      renderTemplatedFile(
        siteConfig,
        pageConfig,
        allUsedLanguages,
        contentFolder = contentFolder,
        file = file,
        outputFolder = outputFolder,
        markdownRenderer,
        templateHandler,
        paginateItems = None,
        dataYamls = dataYamls
      )
    }.toSeq

    // generate index files with pagination and all
    val contentByLangAndCategory: mutable.Map[(String, String), Seq[PageContext]] = (for
      lang <- allUsedLanguages.map(_.toLanguageTag)
      cat <- siteConfig.categories.keys
    yield (lang, cat) -> Seq.empty[PageContext]).to(mutable.Map)
    contentResults.foreach { cr =>
      val segments = cr.page.rootRelPath.segments
      val firstSegment = segments.head
      val key =
        if translationsLangCodes.contains(firstSegment)
        then (firstSegment, segments(1)) // (lang, category)
        else (siteConfig.lang.toLanguageTag, firstSegment) // (default lang, category)
      contentByLangAndCategory.get(key) match {
        case Some(contentPages) =>
          contentByLangAndCategory.update(key, contentPages.appended(cr.page))
        case None =>
        // noop for a top level page without category: about.md etc
      }
    }

    // render index files with pagination
    val indexResults = templatedIndexFiles.flatMap { file =>
      val segments = file.relativeTo(contentFolder).segments
      val firstSegment = segments.head
      val key =
        if translationsLangCodes.contains(firstSegment)
        then (firstSegment, segments(1)) // (lang, category)
        else (siteConfig.lang.toLanguageTag, firstSegment) // (default lang, category)

      val contentTemplateRaw = os.read(file)
      val pageConfig = parseConfig(file, contentTemplateRaw)
      val sortOrder: Ordering[PageContext] = pageConfig.pagination.sort_by match {
        case "publish_date" => Ordering.by(_.publishDate.getOrElse(ZonedDateTime.now().minusYears(1000)))
        case "-publish_date" =>
          Ordering.by((_: PageContext).publishDate.getOrElse(ZonedDateTime.now().minusYears(1000))).reverse
        case "title"  => Ordering.by(_.title)
        case "-title" => Ordering.by((_: PageContext).title).reverse
        case other =>
          throw FlatmarkException(
            s"Unsupported sort order: ${other}. Supported: publish_date, -publish_date, title, -title."
          )
      }
      val categoryItems = contentByLangAndCategory
        .getOrElse(
          key,
          contentByLangAndCategory.filter(_._1._1 == key._1).values.flatten.toSeq // by default use all content pages
        )
        .sorted(using sortOrder)
      renderTemplatedFile(
        siteConfig,
        pageConfig,
        allUsedLanguages,
        contentFolder = contentFolder,
        file = file,
        outputFolder = outputFolder,
        markdownRenderer,
        templateHandler,
        paginateItems = Some(categoryItems),
        dataYamls = dataYamls
      )
    }.toSeq

    // copy static files (e.g. images, css..)
    val staticFolder = siteRootFolder / "static"
    if os.exists(staticFolder) then {
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
    }

    // write search files
    if siteConfig.search.enabled then {
      logger.debug("Generating search entries...")
      val pageSearchEntries = (contentResults ++ indexResults).map { cr =>
        SearchEntry(title = cr.page.title, url = cr.page.url, text = cr.page.text)
      }
      os.write.over(
        outputFolder / "search/entries.json",
        pageSearchEntries.toJson,
        createFolders = true
      )
    }

    logger.info("Site generated successfully")
    true
  } catch {
    case NonFatal(e) =>
      logger.error("Error during site generation", e)
      false
  }

  private def renderTemplatedFile(
      siteConfig: SiteConfig,
      pageConfig: PageConfig,
      languages: Seq[Locale],
      contentFolder: os.Path,
      file: os.Path,
      outputFolder: os.Path,
      markdownRenderer: FlatmarkMarkdownRenderer,
      templateHandler: FlatmarkTemplateHandler,
      paginateItems: Option[Seq[PageContext]],
      dataYamls: Map[String, Node]
  ): Seq[TemplateContext] = {
    logger.debug(s"Rendering templated file: ${file}")
    val baseUrl = siteConfig.base_url.getOrElse("")
    val templateConfig = TemplateConfig(siteConfig, pageConfig)
    val fileRelPath = file.relativeTo(contentFolder)
    val fileExtension = pageConfig.ext.getOrElse("html")
    val locale =
      if fileRelPath.segments.length > 1 && Iso2LanguageCodes(fileRelPath.segments.head) then
        Locale.forLanguageTag(fileRelPath.segments.head)
      else siteConfig.lang
    Locale.setDefault(Locale.Category.DISPLAY, locale) // set locale for rendering language names in current language
    paginateItems match {
      case Some(allItems) if allItems.nonEmpty =>
        val paginatedItems = allItems.grouped(pageConfig.pagination.per_page).toSeq
        for (paginatedItemsGroup, i) <- paginatedItems.zipWithIndex yield {
          def rootRelPath(pageNum: Int): String = {
            val pageNumSuffix = if pageNum == 1 then "" else s"-${pageNum}"
            if fileRelPath.segments.length == 1 then s"${file.baseName}${pageNumSuffix}.${fileExtension}"
            else s"${fileRelPath.segments.init.mkString("/")}/${file.baseName}${pageNumSuffix}.${fileExtension}"
          }
          val contentContext = templateContext(
            languages,
            locale,
            templateConfig,
            defaultLayout = "index.html",
            rootRelPath = n => os.RelPath(rootRelPath(n)),
            getUrl = n => s"${baseUrl}/${rootRelPath(n)}",
            paginatedItemsGroup,
            currentPage = i + 1,
            pageSize = pageConfig.pagination.per_page,
            totalItems = allItems.length,
            dataYamls = dataYamls
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
      case _ =>
        val rootRelPath =
          if fileRelPath.segments.length == 1 then s"${file.baseName}.${fileExtension}"
          else s"${fileRelPath.segments.init.mkString("/")}/${file.baseName}.${fileExtension}"
        val defaultLayout = if paginateItems.isDefined then "index.html" else "page.html"
        val contentContext = templateContext(
          languages,
          locale,
          templateConfig,
          defaultLayout = defaultLayout,
          rootRelPath = _ => os.RelPath(rootRelPath),
          _ => s"${baseUrl}/${rootRelPath}",
          Seq.empty,
          currentPage = 0,
          pageSize = 0,
          totalItems = 0,
          dataYamls = dataYamls
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
  ): TemplateContext = {
    // 1. we first render the content file with basic context (page title, description, etc.)
    // 2. then we feed it to markdown renderer to convert markdown to HTML
    // 3. and finally we render the layout with the content HTML

    val fileRelPath = file.relativeTo(contentFolder)
    val content = templateHandler.render(fileRelPath.toString, contentContext.toJavaContext, locale)
    val contentHtml = if file.ext == "md" then markdownRenderer.renderMarkdown(content) else content
    val jsoupDocument = Jsoup.parse(contentHtml)

    val toc = locally {
      val headingsTree = HeadingHierarchyExtractor.extract(jsoupDocument)
      def toTocItem(heading: HeadingHierarchyExtractor.Heading): TocItemContext =
        TocItemContext(
          heading.level,
          title = heading.text,
          url = s"#${heading.id}",
          children = heading.children.map(toTocItem).toSeq
        )
      headingsTree.map(toTocItem)
    }

    // render final HTML file
    val layoutContext = contentContext.copy(
      page = contentContext.page.copy(
        content = contentHtml,
        text = jsoupDocument.text(),
        toc = toc
      )
    )
    val finalHtml = locally {
      val templatedHtml = templateHandler.render(layoutContext.page.layout, layoutContext.toJavaContext, locale)
      val document = Jsoup.parse(templatedHtml)
      // prepend base URL to all relative URLs
      layoutContext.site.baseUrl.foreach { baseUrl =>
        // TODO handle srcset
        val urlAttrs = List("href", "src", "cite", "action", "formaction", "data", "poster", "manifest")
        urlAttrs.foreach { attrName =>
          document.select(s"""[${attrName}^="/"]""").forEach { elem =>
            val attrValue = elem.attr(attrName)
            elem.attr(attrName, baseUrl + attrValue)
          }
        }
      }
      // add anchor links to headings
      document.select("""h1,h2,h3,h4,h5,h6""").forEach { elem =>
        val id = elem.attr("id").trim
        if id.nonEmpty then elem.append(s"""<a href="#${id}" class="flatmark-anchor" aria-label="Anchor"> 🔗</a>""")
      }
      document.toString
    }

    os.write.over(
      outputFolder / layoutContext.page.rootRelPath,
      finalHtml,
      createFolders = true
    )
    logger.debug(s"Rendered templated file: ${file}")
    layoutContext
  }

  private def templateContext(
      languages: Seq[Locale],
      lang: Locale,
      templateConfig: TemplateConfig,
      defaultLayout: String,
      rootRelPath: Int => os.RelPath,
      getUrl: Int => String,
      items: Seq[PageContext],
      currentPage: Int,
      pageSize: Int,
      totalItems: Int,
      dataYamls: Map[String, Node]
  ): TemplateContext = {
    val (langContexts, langContext) = locally {
      val originalLocale = Locale.getDefault(Locale.Category.DISPLAY)
      val res1 = languages.map { l =>
        // set locale for rendering language names in respective language
        Locale.setDefault(Locale.Category.DISPLAY, l)
        val url = if templateConfig.site.lang == l then "/" else s"/${l.toLanguageTag}"
        LanguageContext(l.toLanguageTag, l.getDisplayLanguage, url)
      }
      Locale.setDefault(Locale.Category.DISPLAY, originalLocale) // restore original locale
      val res2 = LanguageContext(
        lang.toLanguageTag,
        lang.getDisplayLanguage,
        if templateConfig.site.lang == lang then "/" else s"/${lang.toLanguageTag}"
      )
      (res1, res2)
    }

    val data = dataYamls.map { case (key, value) =>
      key -> nodetoJavaContext(value)
    }.asJava

    TemplateContext(
      SiteContext(
        name = templateConfig.site.name,
        description = templateConfig.site.description,
        baseUrl = templateConfig.site.base_url,
        langs = langContexts,
        search = SearchContext(templateConfig.site.search.enabled),
        categories = templateConfig.site.categories.map { case (key, value) =>
          key -> CategoryContext(value.label, value.description)
        },
        tags = templateConfig.site.tags.map { case (key, value) => key -> TagContext(value.label, value.description) },
        codeHighlight = CodeHighlightContext(templateConfig.site.code_highlight.enabled),
        mathHighlight = MathHighlightContext(templateConfig.site.math_highlight.enabled),
        data = data
      ),
      PageContext(
        layout = templateConfig.page.layout.getOrElse(defaultLayout),
        title = templateConfig.page.title,
        description = templateConfig.page.description,
        content = "", // filled in later from markdown-rendered-HTML
        text = "", // filled in later from markdown-rendered-HTML
        lang = langContext,
        publishDate = templateConfig.page.publish_date.map(_.atZone(templateConfig.site.timezone.toZoneId)),
        rootRelPath = rootRelPath(currentPage),
        url = getUrl(currentPage),
        themeProps = templateConfig.page.theme_props,
        toc = Seq.empty // filled in later from markdown-rendered-HTML
      ),
      Option.when(items.nonEmpty)(
        PaginatorContext(
          currentPage = currentPage,
          items = items,
          totalItems = totalItems,
          pageSize = pageSize,
          getUrl = getUrl
        )
      )
    )
  }

  private def nodetoJavaContext(node: Node): Object = node match {
    case sn: Node.ScalarNode =>
      if sn.tag == Tag.nullTag then null
      else if sn.tag == Tag.int then Integer.valueOf(sn.value.toInt)
      else if sn.tag == Tag.float then Double.box(sn.value.toDouble)
      else if sn.tag == Tag.boolean then Boolean.box(sn.value.toBoolean)
      else sn.value
    case mn: Node.MappingNode =>
      mn.mappings.map { case (key, value) => nodetoJavaContext(key) -> nodetoJavaContext(value) }.asJava
    case sn: Node.SequenceNode => sn.nodes.map(nodetoJavaContext).asJava
  }
}

class FlatmarkException(message: String, cause: Throwable = null) extends RuntimeException(message, cause)
