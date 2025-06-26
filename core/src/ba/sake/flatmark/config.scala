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
    theme: String = "https://github.com/sake92/flatmark?branch=main&folder=themes%2Fdefault",
    categories: ListMap[String, CategoryConfig] = ListMap.empty,
    tags: ListMap[String, TagConfig] = ListMap.empty,
    highlight_code: Boolean = true,
    highlight_math: Boolean = true
) derives YamlCodec

case class CategoryConfig(
    label: String,
    description: String = ""
) derives YamlCodec

case class TagConfig(
    label: String,
    description: String = ""
) derives YamlCodec

case class PageConfig(
    layout: Option[String] = None,
    title: String = "Untitled",
    description: String = "",
    publish_date: Option[LocalDateTime] = None,
    ext: Option[String] = None,
    theme_props: Map[String, String] = Map.empty
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
