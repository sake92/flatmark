package ba.sake.flatmark

import java.time.LocalDateTime
import java.util.{Locale, TimeZone}
import scala.collection.immutable.ListMap
import scala.util.boundary
import org.virtuslab.yaml.*
import YamlInstances.given

/* * Flatmark configuration classes.
 * These classes are used to parse the YAML front matter in Markdown files and the site configuration.
 */

case class TemplateConfig(
    site: SiteConfig, // _config.yaml
    page: PageConfig // page yaml front matter
) derives YamlCodec

// use all snake case props!
case class SiteConfig(
    name: String = "My Site",
    description: String = "",
    base_url: Option[String] = None,
    lang: Locale = Locale.ENGLISH, // Default language
    timezone: TimeZone = TimeZone.getDefault,
    theme: ThemeConfig = ThemeConfig(),
    search: SearchConfig = SearchConfig(),
    categories: ListMap[String, CategoryConfig] = ListMap.empty,
    tags: ListMap[String, TagConfig] = ListMap.empty,
    code_highlight: CodeHighlightConfig = CodeHighlightConfig(),
    math_highlight: MathHighlightConfig = MathHighlightConfig()
) derives YamlCodec

case class ThemeConfig(
    source: String = "https://github.com/sake92/flatmark-themes?branch=main&folder=default",
    enabled: Boolean = true
) derives YamlCodec

case class SearchConfig(
    enabled: Boolean = true
) derives YamlCodec

case class CategoryConfig(
    label: String,
    description: String = ""
) derives YamlCodec

case class TagConfig(
    label: String,
    description: String = ""
) derives YamlCodec

case class CodeHighlightConfig(
    enabled: Boolean = true
) derives YamlCodec

case class MathHighlightConfig(
    enabled: Boolean = true
) derives YamlCodec

case class PageConfig(
    layout: Option[String] = None,
    title: String = "Untitled",
    description: String = "",
    publish_date: Option[LocalDateTime] = None,
    ext: Option[String] = None,
    pagination: PaginationConfig = PaginationConfig(),
    theme_props: Map[String, String] = Map.empty
) derives YamlCodec

case class PaginationConfig(
    enabled: Boolean = false,
    per_page: Int = 10,
    sort_by: String = "-publish_date"
) derives YamlCodec

// TODO pass in just YAML
private[flatmark] def parseConfig(file: os.Path, mdTemplateRaw: String): PageConfig = {
  val (rawYaml, _) = FrontMatterUtils.extract(mdTemplateRaw)
  if rawYaml.trim.isEmpty then PageConfig()
  else
    rawYaml
      .as[PageConfig]
      .left
      .map { error =>
        throw FlatmarkException(s"Failed to parse YAML front matter in file '$file': ${error.getMessage}")
      }
      .getOrElse(PageConfig())
}
