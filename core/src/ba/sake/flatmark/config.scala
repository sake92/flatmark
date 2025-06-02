package ba.sake.flatmark

import scala.util.boundary
import org.virtuslab.yaml.*

/* * Flatmark configuration classes.
 * These classes are used to parse the YAML front matter in Markdown files and the site configuration.
 */

case class TemplateConfig(site: SiteConfig, page: PageConfig) derives YamlCodec

case class SiteConfig(
    name: String = "My Site",
    description: String = "",
    baseUrl: String = "",
    lang: String = "en", // Default language
    theme: String = "default"
) derives YamlCodec

case class PageConfig(
    layout: String = "default",
    title: String = "Untitled",
    description: String = "",
    content: String = ""
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
    rawYaml.as[PageConfig].toOption.getOrElse {
      throw RuntimeException(s"Invalid YAML front matter in file: ${fileNameBase}. Expected PageConfig format.")
    }
  } else PageConfig()
}
