package ba.sake.flatmark.generation

import ba.sake.flatmark.*

import java.time.{LocalDateTime, ZoneOffset, ZonedDateTime}
import java.util.Locale
import scala.collection.immutable.ListMap

private[generation] final case class ContentRoute(
    file: os.Path,
    language: Locale,
    logicalPath: String,
    url: String
)

private[generation] final class SitePlan private[generation] (
    val contentFiles: Seq[os.Path],
    val indexFiles: Seq[os.Path],
    val pageConfigs: Map[os.Path, PageConfig],
    val usedLanguages: Seq[Locale],
    private val siteConfig: SiteConfig,
    private val translationsLanguageCodes: Set[String],
    private val routesByFile: Map[os.Path, ContentRoute],
    private val routesByLanguageAndLogicalPath: Map[(String, String), ContentRoute]
) {
  def pageConfig(file: os.Path): PageConfig = pageConfigs(file)
  def route(file: os.Path): ContentRoute = routesByFile(file)

  def languageRoutes(file: os.Path): Seq[(Locale, String)] = {
    val currentRoute = route(file)
    usedLanguages.map { language =>
      val url = routesByLanguageAndLogicalPath
        .get(language.toLanguageTag -> currentRoute.logicalPath)
        .map(_.url)
        .getOrElse(languageHomeUrl(language))
      language -> url
    }
  }

  def categoryContexts(
      language: Locale,
      contentByCategory: Map[String, Seq[PageContext]] = Map.empty
  ): ListMap[String, CategoryContext] =
    siteConfig.categories.flatMap { case (key, config) =>
      Option.when(routesByLanguageAndLogicalPath.contains(language.toLanguageTag -> s"${key}/index")) {
        key -> CategoryContext(config.label, config.description, contentByCategory.getOrElse(key, Seq.empty))
      }
    }

  def contentByLanguageAndCategory(results: Seq[TemplateContext]): Map[(String, String), Seq[PageContext]] = {
    val empty = (for
      language <- usedLanguages.map(_.toLanguageTag)
      category <- siteConfig.categories.keys
    yield (language, category) -> Seq.empty[PageContext]).toMap

    results.foldLeft(empty) { (content, result) =>
      val segments = result.page.rootRelPath.segments
      val firstSegment = segments.head
      val key =
        if translationsLanguageCodes(firstSegment) then firstSegment -> segments(1)
        else siteConfig.lang.toLanguageTag -> firstSegment
      content.updatedWith(key)(_.map(_.appended(result.page)))
    }
  }

  def categoryContextsByLanguage(
      content: Map[(String, String), Seq[PageContext]]
  ): Map[String, ListMap[String, CategoryContext]] =
    usedLanguages.map { language =>
      val contentByCategory = siteConfig.categories.keys.map { category =>
        category -> content.getOrElse(language.toLanguageTag -> category, Seq.empty)
      }.toMap
      language.toLanguageTag -> categoryContexts(language, contentByCategory)
    }.toMap

  def itemsForIndex(file: os.Path, content: Map[(String, String), Seq[PageContext]]): Seq[PageContext] = {
    val fileRoute = route(file)
    val category = fileRoute.logicalPath.split('/').head
    val key = fileRoute.language.toLanguageTag -> category
    val items = content.getOrElse(
      key,
      content.iterator.collect { case ((language, _), pages) if language == key._1 => pages }.flatten.toSeq
    )
    val missingDate = LocalDateTime.MIN.atZone(ZoneOffset.UTC)
    pageConfig(file).pagination.sort_by match {
      case "publish_date"  => items.sortBy(_.publishDate.getOrElse(missingDate))
      case "-publish_date" => items.sortBy(_.publishDate.getOrElse(missingDate))(using Ordering[ZonedDateTime].reverse)
      case "title"         => items.sortBy(_.title)
      case "-title"        => items.sortBy(_.title)(using Ordering[String].reverse)
    }
  }

  private def languageHomeUrl(language: Locale): String =
    if siteConfig.lang == language then "/" else s"/${language.toLanguageTag}"
}

private[generation] object SitePlanner {
  private val Iso2LanguageCodes = Set(Locale.getISOLanguages*)

  def plan(sources: SiteSources, siteConfig: SiteConfig): SitePlan = {
    val routes =
      sources.files.map(file => contentRoute(sources.contentFolder, file, sources.pageConfigs(file), siteConfig.lang))
    val duplicateRoutes = routes
      .groupBy(route => route.language.toLanguageTag -> route.logicalPath)
      .values
      .filter(_.sizeIs > 1)
      .toSeq
    if duplicateRoutes.nonEmpty then
      throw FlatmarkException(
        s"Multiple content files define the same language and route: ${duplicateRoutes.flatten.map(_.file).mkString(", ")}"
      )

    val translationLanguages = sources.files.iterator
      .map(_.relativeTo(sources.contentFolder).segments)
      .collect { case segments if segments.length > 1 && Iso2LanguageCodes(segments.head) => segments.head }
      .toSet
    val usedLanguages =
      (siteConfig.lang.toLanguageTag :: translationLanguages.toList).distinct.sorted.map(Locale.forLanguageTag)
    val (indexFiles, contentFiles) = sources.files.partition(_.baseName == "index")
    new SitePlan(
      contentFiles,
      indexFiles,
      sources.pageConfigs,
      usedLanguages,
      siteConfig,
      translationLanguages,
      routes.map(route => route.file -> route).toMap,
      routes.map(route => (route.language.toLanguageTag -> route.logicalPath) -> route).toMap
    )
  }

  private def contentRoute(
      contentFolder: os.Path,
      file: os.Path,
      pageConfig: PageConfig,
      defaultLanguage: Locale
  ): ContentRoute = {
    val relativePath = file.relativeTo(contentFolder)
    val isTranslation = relativePath.segments.length > 1 && Iso2LanguageCodes(relativePath.segments.head)
    val language = if isTranslation then Locale.forLanguageTag(relativePath.segments.head) else defaultLanguage
    val logicalSegments =
      (if isTranslation then relativePath.segments.tail else relativePath.segments).init.appended(file.baseName)
    val extension = pageConfig.ext.getOrElse("html")
    val outputSegments = relativePath.segments.init.appended(s"${file.baseName}.${extension}")
    val url =
      if file.baseName == "index" && extension == "html" then {
        val folderUrl = outputSegments.init.mkString("/")
        if folderUrl.isEmpty then "/" else s"/${folderUrl}"
      } else s"/${outputSegments.mkString("/")}"
    ContentRoute(file, language, logicalSegments.mkString("/"), url)
  }
}
