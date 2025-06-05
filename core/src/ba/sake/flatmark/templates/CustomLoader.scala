package ba.sake.flatmark.templates

import io.pebbletemplates.pebble.error.LoaderException
import io.pebbletemplates.pebble.loader.FileLoader
import io.pebbletemplates.pebble.utils.PathUtils

import java.io.{File, Reader, StringReader}
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import scala.util.boundary

// skips YAML front matter (triple dash lines) in the content files
class CustomLoader extends FileLoader {

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
    // add the prefix and ensure the prefix ends with a separator character
    val path = new StringBuilder
    if (this.getPrefix != null) {
      path.append(this.getPrefix)
      if (!this.getPrefix.endsWith(String.valueOf(File.separatorChar))) path.append(File.separatorChar)
    }
    var templateName = originalTemplateName + (if (this.getSuffix == null) "" else this.getSuffix)
    /*
     * if template name contains path segments, move those segments into the
     * path variable. The below technique needs to know the difference
     * between the path and file name.
     */
    val pathSegments = PathUtils.PATH_SEPARATOR_REGEX.split(templateName)
    if (pathSegments.length > 1) {
      // file name is the last segment
      templateName = pathSegments(pathSegments.length - 1)
    }
    for (i <- 0 until (pathSegments.length - 1)) {
      path.append(pathSegments(i)).append(File.separatorChar)
    }
    // try to load File
    new File(path.toString, templateName)
  }
}
