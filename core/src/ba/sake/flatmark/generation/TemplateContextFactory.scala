package ba.sake.flatmark.generation

import ba.sake.flatmark.*
import org.virtuslab.yaml.{Node, Tag}

import java.util.Locale
import scala.collection.immutable.ListMap
import scala.jdk.CollectionConverters.*

private[generation] final class TemplateContextFactory(siteConfig: SiteConfig, dataYamls: Map[String, Node]) {
  def create(
      languageRoutes: Seq[(Locale, String)],
      language: Locale,
      pageConfig: PageConfig,
      defaultLayout: String,
      rootRelPath: Int => os.RelPath,
      getUrl: Int => String,
      items: Option[Seq[PageContext]],
      currentPage: Int,
      totalItems: Int,
      categoryContexts: ListMap[String, CategoryContext]
  ): TemplateContext = {
    val languageContexts = languageRoutes.map { case (locale, url) =>
      LanguageContext(locale.toLanguageTag, locale.getDisplayLanguage(locale), url)
    }
    val currentLanguage = LanguageContext(
      language.toLanguageTag,
      language.getDisplayLanguage(language),
      if siteConfig.lang == language then "/" else s"/${language.toLanguageTag}"
    )
    val data = dataYamls.map { case (key, value) => key -> nodeToJavaContext(value) }.asJava
    TemplateContext(
      SiteContext(
        siteConfig.name,
        siteConfig.description,
        siteConfig.base_url,
        languageContexts,
        SearchContext(siteConfig.search.enabled),
        categoryContexts,
        siteConfig.tags.map { case (key, value) => key -> TagContext(value.label, value.description) },
        CodeHighlightContext(siteConfig.code_highlight.enabled),
        MathHighlightContext(siteConfig.math_highlight.enabled),
        data
      ),
      PageContext(
        pageConfig.layout.getOrElse(defaultLayout),
        pageConfig.title,
        pageConfig.description,
        content = "",
        text = "",
        currentLanguage,
        pageConfig.publish_date.map(_.atZone(siteConfig.timezone.toZoneId)),
        rootRelPath(currentPage),
        getUrl(currentPage),
        pageConfig.theme_props,
        toc = Seq.empty
      ),
      items.map(pageItems =>
        PaginatorContext(
          pageConfig.pagination.enabled,
          currentPage,
          pageItems,
          totalItems,
          pageConfig.pagination.per_page,
          getUrl
        )
      )
    )
  }

  private def nodeToJavaContext(node: Node): Object = node match {
    case scalar: Node.ScalarNode =>
      if scalar.tag == Tag.nullTag then null
      else if scalar.tag == Tag.int then Integer.valueOf(scalar.value.toInt)
      else if scalar.tag == Tag.float then Double.box(scalar.value.toDouble)
      else if scalar.tag == Tag.boolean then Boolean.box(scalar.value.toBoolean)
      else scalar.value
    case mapping: Node.MappingNode =>
      mapping.mappings.map { case (key, value) => nodeToJavaContext(key) -> nodeToJavaContext(value) }.asJava
    case sequence: Node.SequenceNode => sequence.nodes.map(nodeToJavaContext).asJava
  }
}
