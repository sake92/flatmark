package ba.sake.flatmark.generation

import ba.sake.flatmark.*
import ba.sake.flatmark.markdown.FlatmarkMarkdownRenderer
import ba.sake.flatmark.templates.FlatmarkTemplateHandler
import org.jsoup.Jsoup
import org.slf4j.LoggerFactory
import org.virtuslab.yaml.Node

import java.util.Locale
import scala.collection.immutable.ListMap

private[generation] final class PageRenderer(
    siteConfig: SiteConfig,
    contentFolder: os.Path,
    outputFolder: os.Path,
    markdownRenderer: FlatmarkMarkdownRenderer,
    templateHandler: FlatmarkTemplateHandler,
    dataYamls: Map[String, Node]
) {
  private val logger = LoggerFactory.getLogger(getClass.getName)
  private val UrlAttributes = Seq("href", "src", "cite", "action", "formaction", "data", "poster", "manifest")
  private val contextFactory = TemplateContextFactory(siteConfig, dataYamls)

  def render(
      file: os.Path,
      pageConfig: PageConfig,
      language: Locale,
      languageRoutes: Seq[(Locale, String)],
      paginateItems: Option[Seq[PageContext]],
      categoryContexts: ListMap[String, CategoryContext]
  ): Seq[TemplateContext] = {
    logger.debug(s"Rendering templated file: ${file}")
    val relativePath = file.relativeTo(contentFolder)
    val extension = pageConfig.ext.getOrElse("html")

    paginateItems match {
      case Some(items) if pageConfig.pagination.enabled && items.nonEmpty =>
        items.grouped(pageConfig.pagination.per_page).toSeq.zipWithIndex.map { case (pageItems, index) =>
          renderPage(
            file,
            language,
            languageRoutes,
            pageConfig,
            defaultLayout = "index.html",
            rootRelPath = page => paginatedPath(relativePath, file.baseName, extension, page),
            items = Some(pageItems),
            currentPage = index + 1,
            totalItems = items.size,
            categoryContexts
          )
        }
      case Some(items) =>
        Seq(
          renderPage(
            file,
            language,
            languageRoutes,
            pageConfig,
            defaultLayout = "index.html",
            _ => outputPath(relativePath, file.baseName, extension),
            items = Some(items),
            currentPage = 1,
            totalItems = items.size,
            categoryContexts
          )
        )
      case None =>
        Seq(
          renderPage(
            file,
            language,
            languageRoutes,
            pageConfig,
            defaultLayout = "page.html",
            _ => outputPath(relativePath, file.baseName, extension),
            items = None,
            currentPage = 1,
            totalItems = 0,
            categoryContexts
          )
        )
    }
  }

  private def renderPage(
      file: os.Path,
      language: Locale,
      languageRoutes: Seq[(Locale, String)],
      pageConfig: PageConfig,
      defaultLayout: String,
      rootRelPath: Int => os.RelPath,
      items: Option[Seq[PageContext]],
      currentPage: Int,
      totalItems: Int,
      categoryContexts: ListMap[String, CategoryContext]
  ): TemplateContext = {
    val baseUrl = siteConfig.base_url.getOrElse("")
    val context = contextFactory.create(
      languageRoutes,
      language,
      pageConfig,
      defaultLayout,
      rootRelPath,
      page => s"${baseUrl}/${rootRelPath(page)}",
      items,
      currentPage,
      totalItems,
      categoryContexts
    )
    renderSingle(file, context, language)
  }

  private def renderSingle(file: os.Path, contentContext: TemplateContext, locale: Locale): TemplateContext = {
    val relativePath = file.relativeTo(contentFolder)
    val content = templateHandler.render(relativePath.toString, contentContext.toJavaContext, locale)
    val contentHtml = if file.ext == "md" then markdownRenderer.renderMarkdown(content) else content
    val contentDocument = Jsoup.parse(contentHtml)
    val toc = HeadingHierarchyExtractor.extract(contentDocument).map(toTocItem)
    val layoutContext = contentContext.copy(
      page = contentContext.page.copy(content = contentHtml, text = contentDocument.text(), toc = toc)
    )

    val finalHtml = {
      val document = Jsoup.parse(templateHandler.render(layoutContext.page.layout, layoutContext.toJavaContext, locale))
      layoutContext.site.baseUrl.foreach { baseUrl =>
        UrlAttributes.foreach { attribute =>
          document.select(s"""[${attribute}^="/"]""").forEach { element =>
            element.attr(attribute, baseUrl + element.attr(attribute))
          }
        }
      }
      document.select("h1,h2,h3,h4,h5,h6").forEach { element =>
        val id = element.attr("id").trim
        if id.nonEmpty then element.append(s"""<a href="#${id}" class="flatmark-anchor" aria-label="Anchor"> 🔗</a>""")
      }
      document.toString
    }

    os.write.over(outputFolder / layoutContext.page.rootRelPath, finalHtml, createFolders = true)
    logger.debug(s"Rendered templated file: ${file}")
    layoutContext
  }

  private def outputPath(relativePath: os.RelPath, baseName: String, extension: String): os.RelPath =
    if relativePath.segments.length == 1 then os.RelPath(s"${baseName}.${extension}")
    else os.RelPath(s"${relativePath.segments.init.mkString("/")}/${baseName}.${extension}")

  private def paginatedPath(
      relativePath: os.RelPath,
      baseName: String,
      extension: String,
      page: Int
  ): os.RelPath = {
    val suffix = if page == 1 then "" else s"-${page}"
    if relativePath.segments.length == 1 then os.RelPath(s"${baseName}${suffix}.${extension}")
    else os.RelPath(s"${relativePath.segments.init.mkString("/")}/${baseName}${suffix}.${extension}")
  }

  private def toTocItem(heading: HeadingHierarchyExtractor.Heading): TocItemContext =
    TocItemContext(heading.level, heading.text, s"#${heading.id}", heading.children.map(toTocItem).toSeq)

}
