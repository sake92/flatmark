package ba.sake.flatmark

import java.time.LocalDateTime
import java.util.{Locale, TimeZone}
import scala.util.boundary
import org.virtuslab.yaml.*
import YamlInstances.given

/* * Flatmark configuration classes.
 * These classes are used to parse the YAML front matter in Markdown files and the site configuration.
 */

case class TemplateConfig(
    site: SiteConfig,
    page: PageConfig
) derives YamlCodec

// theme: https://github.com/sake92/my_theme?branch=main&folder=my_folder (po defaultu main branch i root folder uzet)
// ako ne postoji .flatmark-cache/themes/md5(https://github.com/sake92/my_theme?branch=main&folder=my_folder)
// probat u ovom redoslijedu:
//    - git clone https://github.com/sake92/my_theme.git
//    - git clone git@github.com:sake92/my_theme.git (SSH)
//    - CURL https://api.github.com/repos/sake92/my_theme/zipball/REF (ako folder ne postoji)
// i onda unzippat u .flatmark-cache/themes/
case class SiteConfig(
    name: String = "My Site",
    description: String = "",
    baseUrl: String = "",
    lang: Locale = Locale.ENGLISH, // Default language
    timezone: TimeZone = TimeZone.getDefault,
    theme: String = "https://github.com/sake92/flatmark?branch=main&folder=themes%2Fdefault",
    categories: Map[String, CategoryConfig] = Map.empty,
    tags: Map[String, TagConfig] = Map.empty
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
    layout: String = "page",
    title: String = "Untitled",
    description: String = "",
    publishDate: Option[LocalDateTime] = None
) derives YamlCodec

private[flatmark] def parseConfig(fileNameBase: String, mdTemplateRaw: String): PageConfig = {
  var hasYamlFrontMatter = false
  var firstTripleDashIndex = -1
  var secondTripleDashIndex = -1
  boundary {
    val iter = mdTemplateRaw.linesIterator
    var i = 0
    while iter.hasNext do {
      val line = iter.next().trim
      if line.nonEmpty then {
        if line == "---" then {
          if (firstTripleDashIndex == -1) firstTripleDashIndex = i
          else if (secondTripleDashIndex == -1) {
            secondTripleDashIndex = i
            hasYamlFrontMatter = true
            boundary.break()
          }
        } else if (firstTripleDashIndex == -1) {
          boundary.break() // first non-empty line is not triple dash -> no YAML front matter
        }
      }
      i += 1
    }
  }
  if hasYamlFrontMatter then {
    val rawYaml = mdTemplateRaw.linesIterator
      .slice(firstTripleDashIndex + 1, firstTripleDashIndex + 1 + secondTripleDashIndex - firstTripleDashIndex - 1)
      .mkString("\n")
    rawYaml
      .as[PageConfig]
      .left
      .map { error =>
        throw FlatmarkException(s"Failed to parse YAML front matter in file '$fileNameBase': ${error.getMessage}")
      }
      .getOrElse(PageConfig())
  } else PageConfig()
}
