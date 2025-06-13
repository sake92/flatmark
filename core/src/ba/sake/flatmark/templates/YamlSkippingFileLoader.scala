package ba.sake.flatmark.templates

import java.io.{File, Reader, StringReader}
import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Path}
import scala.util.boundary
import io.pebbletemplates.pebble.error.LoaderException
import io.pebbletemplates.pebble.loader.FileLoader

class YamlSkippingFileLoader(rootFolder: Path) extends FileLoader {

  override def getReader(templateName: String): Reader = {
    val file = getFile(templateName)
    if !file.exists || !file.isFile then {
      throw LoaderException(null, s"Could not find template '${templateName}'")
    }

    val rawContent = Files.readString(file.toPath, StandardCharsets.UTF_8)
    var hasYamlFrontMatter = false
    var firstTripleDashIndex = -1
    var secondTripleDashIndex = -1
    boundary {
      val iter = rawContent.linesIterator
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
    val rawContentNoYaml = if hasYamlFrontMatter then {
      rawContent.linesIterator
        .drop(secondTripleDashIndex + 1)
        .mkString("\n")
        .trim
    } else {
      rawContent
    }
    new StringReader(rawContentNoYaml)
  }

  private def getFile(originalTemplateName: String) = {
    val normalizedPrefix = Option(getPrefix)
      .getOrElse("")
      .dropWhile(_ == File.separatorChar)
      .reverse
      .dropWhile(_ == File.separatorChar)
      .reverse
    val normalizedTemplateName =
      originalTemplateName.dropWhile(_ == File.separatorChar).reverse.dropWhile(_ == File.separatorChar).reverse
    val normalizedSuffix = Option(getSuffix).getOrElse("")
    val templateName = (if normalizedPrefix.isEmpty then "" else s"${normalizedPrefix}/") +
      normalizedTemplateName + (if normalizedSuffix.isEmpty then "" else normalizedSuffix)
    rootFolder.resolve(templateName).toFile
  }
}
