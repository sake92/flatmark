package ba.sake.flatmark.generation

import ba.sake.flatmark.*
import ba.sake.flatmark.markdown.FlatmarkMarkdownRenderer
import ba.sake.flatmark.search.SearchEntry
import ba.sake.flatmark.ssr.{FlatmarkSsr, WebDriverHolder}
import ba.sake.flatmark.templates.FlatmarkTemplateHandler
import ba.sake.tupson.{JsonRW, toJson}
import org.slf4j.LoggerFactory

import java.net.URLClassLoader
import scala.util.control.NonFatal

final class FlatmarkGenerator(ssrServerUrl: String, webDriverHolder: WebDriverHolder, updateTheme: Boolean) {
  private val logger = LoggerFactory.getLogger(getClass.getName)

  def generate(siteRootFolder: os.Path, useCache: Boolean): Boolean =
    try {
      generateOrThrow(siteRootFolder, useCache)
      logger.info("Site generated successfully")
      true
    } catch {
      case NonFatal(e) =>
        logger.error("Error during site generation", e)
        false
    }

  private def generateOrThrow(siteRootFolder: os.Path, useCache: Boolean): Unit = {
    logger.info(s"Generating site in '${siteRootFolder}'")
    if !os.exists(siteRootFolder) then throw FlatmarkException(s"Site root folder does not exist: ${siteRootFolder}")
    if !os.isDir(siteRootFolder) then throw FlatmarkException(s"Site root is not a folder: ${siteRootFolder}")

    val siteConfig = SiteConfigLoader.load(siteRootFolder)
    val sources = SiteSourceLoader.load(siteRootFolder)
    val plan = SitePlanner.plan(sources, siteConfig)

    val cacheFolder = siteRootFolder / ".flatmark-cache"
    val themeFolder = Option.when(siteConfig.theme.enabled)(
      ThemeResolver.resolve(
        siteConfig.theme.source,
        siteRootFolder / "_themes",
        cacheFolder / "themes",
        updateTheme
      )
    )
    val fileCache = FileCache(cacheFolder, useCache)
    val markdownRenderer = FlatmarkMarkdownRenderer(
      CachingFlatmarkSsr(FlatmarkSsr(ssrServerUrl, webDriverHolder), fileCache)
    )
    val templateClassLoader = new URLClassLoader(
      (Array(siteRootFolder / "_i18n") ++ themeFolder.map(_ / "_i18n").toArray).map(_.toIO.toURI.toURL),
      Thread.currentThread.getContextClassLoader
    )

    try
      TransactionalSiteOutput.publish(siteRootFolder) { outputFolder =>
        val templateHandler =
          FlatmarkTemplateHandler(templateClassLoader, siteRootFolder, themeFolder, markdownRenderer)
        val renderer = PageRenderer(
          siteConfig,
          sources.contentFolder,
          outputFolder,
          markdownRenderer,
          templateHandler,
          sources.dataYamls
        )
        val assets = AssetPublisher(siteRootFolder, outputFolder, themeFolder)

        assets.publishThemeStatic()
        assets.compileSass()

        val contentResults = plan.contentFiles.flatMap { file =>
          val route = plan.route(file)
          renderer.render(
            file,
            plan.pageConfig(file),
            route.language,
            plan.languageRoutes(file),
            paginateItems = None,
            plan.categoryContexts(route.language)
          )
        }

        val contentByLanguageAndCategory = plan.contentByLanguageAndCategory(contentResults)
        val categoryContextsByLanguage = plan.categoryContextsByLanguage(contentByLanguageAndCategory)
        val indexResults = plan.indexFiles.flatMap { file =>
          val route = plan.route(file)
          renderer.render(
            file,
            plan.pageConfig(file),
            route.language,
            plan.languageRoutes(file),
            paginateItems = Some(plan.itemsForIndex(file, contentByLanguageAndCategory)),
            categoryContextsByLanguage(route.language.toLanguageTag)
          )
        }

        assets.publishSiteStatic()
        if siteConfig.search.enabled then {
          val searchLayout = "_layouts/search-results.html"
          val hasSearchLayout = os.exists(siteRootFolder / os.RelPath(searchLayout)) ||
            themeFolder.exists(folder => os.exists(folder / os.RelPath(searchLayout)))
          if hasSearchLayout && !os.exists(outputFolder / "search/results.html") then
            renderer.renderSearchPage(plan.categoryContexts(siteConfig.lang))

          val entries = (contentResults ++ indexResults).map { result =>
            SearchEntry(title = result.page.title, url = result.page.url, text = result.page.text)
          }
          os.write.over(outputFolder / "search/entries.json", entries.toJson, createFolders = true)
        }
      }
    finally templateClassLoader.close()
  }
}
