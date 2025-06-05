package ba.sake.flatmark.templates

import io.pebbletemplates.pebble.PebbleEngine
import io.pebbletemplates.pebble.loader.{DelegatingLoader, FileLoader}
import java.io.StringWriter
import java.util as ju
import java.util.Locale
import java.util.logging.Logger


class FlatmarkTemplateHandler(siteRootFolder: os.Path) {
  private val logger = Logger.getLogger(getClass.getName)

  private val engine = locally {
    val layoutsLoader = new FileLoader()
    val relPath = siteRootFolder.relativeTo(os.pwd).toString
    val relPathPrefix = if (relPath.isEmpty) "" else s"${relPath}/"
    layoutsLoader.setPrefix(s"${relPathPrefix}_layouts/")
    layoutsLoader.setSuffix(".peb")
    val includesLoader = new FileLoader()
    includesLoader.setPrefix(s"${relPathPrefix}_includes/")
    includesLoader.setSuffix(".peb")
    val contentLoader = new CustomLoader()
    contentLoader.setPrefix(s"${relPathPrefix}content/")
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
