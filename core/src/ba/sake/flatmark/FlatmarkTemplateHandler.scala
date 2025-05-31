package ba.sake.flatmark

import java.util as ju
import java.util.logging.Logger
import java.io.StringWriter
import io.pebbletemplates.pebble.PebbleEngine
import io.pebbletemplates.pebble.loader.{FileLoader, StringLoader}

class FlatmarkTemplateHandler(siteRootFolder: os.Path) {
  private val logger = Logger.getLogger(getClass.getName)

  private val layoutLoader = new FileLoader()
  layoutLoader.setPrefix(siteRootFolder.relativeTo(os.pwd).toString + "/")
  layoutLoader.setSuffix(".html")
  private val layoutEngine = new PebbleEngine.Builder().loader(layoutLoader).autoEscaping(false).build()

  private val contentLoader = new StringLoader
  private val contentEngine = new PebbleEngine.Builder().loader(contentLoader).autoEscaping(false).build()

  def renderLayout(templateName: String, context: ju.Map[String, Object]): String = {
    logger.fine(s"Rendering layout with context: ${context}")
    val compiledTemplate = layoutEngine.getTemplate(s"_layouts/${templateName}")
    val writer = new StringWriter()
    compiledTemplate.evaluate(writer, context)
    writer.toString
  }

  def renderContent(templateValue: String, context: ju.Map[String, Object]): String = {
    logger.fine(s"Rendering content with context: ${context}")
    // template name is actually the value of the template in StringLoader
    val compiledTemplate = contentEngine.getTemplate(templateValue)
    val writer = new StringWriter()
    compiledTemplate.evaluate(writer, context)
    writer.toString
  }
}
