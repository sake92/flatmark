package ba.sake.flatmark

import java.nio.file.Files
import java.nio.charset.StandardCharsets
import java.util as ju
import java.util.logging.Logger
import java.util.Locale
import java.io.{File, Reader, StringReader, StringWriter}
import scala.util.boundary
import io.pebbletemplates.pebble.PebbleEngine
import io.pebbletemplates.pebble.loader.{DelegatingLoader, FileLoader}
import io.pebbletemplates.pebble.utils.PathUtils
import io.pebbletemplates.pebble.attributes.AttributeResolver
import io.pebbletemplates.pebble.error.LoaderException

class FlatmarkTemplateHandler(siteRootFolder: os.Path) {
  private val logger = Logger.getLogger(getClass.getName)

  private val engine = locally {
    val layoutsLoader = new FileLoader()
    layoutsLoader.setPrefix(siteRootFolder.relativeTo(os.pwd).toString + "/_layouts/")
    layoutsLoader.setSuffix(".peb")
    val includesLoader = new FileLoader()
    includesLoader.setPrefix(siteRootFolder.relativeTo(os.pwd).toString + "/_includes/")
    includesLoader.setSuffix(".peb")
    val contentLoader = new CustomLoader()
    contentLoader.setPrefix(siteRootFolder.relativeTo(os.pwd).toString + "/content/")
    val loader = new DelegatingLoader(ju.Arrays.asList(contentLoader, layoutsLoader, includesLoader))
    new PebbleEngine.Builder().loader(loader).autoEscaping(false).build()
  }

  def render(templateName: String, context: ju.Map[String, Object], locale: Locale): String = {
    logger.fine(s"Rendering '${templateName}' with context: ${context}")
    val compiledTemplate = engine.getTemplate(templateName)
    val writer = new StringWriter()
    compiledTemplate.evaluate(writer, context, locale)
    writer.toString
  }

}

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
