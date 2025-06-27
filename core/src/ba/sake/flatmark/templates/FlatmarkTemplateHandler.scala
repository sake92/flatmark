package ba.sake.flatmark.templates

import java.io.StringWriter
import java.util as ju
import java.util.Locale
import scala.jdk.CollectionConverters.*
import org.slf4j.LoggerFactory
import io.pebbletemplates.pebble.PebbleEngine
import io.pebbletemplates.pebble.loader.DelegatingLoader
import ba.sake.flatmark.templates.i18n.I18nExtension

class FlatmarkTemplateHandler(flatmarkClassLoader: ClassLoader, siteRootFolder: os.Path, themeFolder: Option[os.Path]) {

  private val logger = LoggerFactory.getLogger(getClass.getName)

  private val engine = locally {
    // local layouts,includes
    val layoutsLoader = new YamlSkippingFileLoader(siteRootFolder.wrapped)
    layoutsLoader.setPrefix("_layouts")
    layoutsLoader.setSuffix(".peb")
    val includesLoader = new YamlSkippingFileLoader(siteRootFolder.wrapped)
    includesLoader.setPrefix("_includes")
    includesLoader.setSuffix(".peb")
    val contentLoader = new YamlSkippingFileLoader(siteRootFolder.wrapped)
    contentLoader.setPrefix("content")
    val rootLoaders = List(layoutsLoader, includesLoader, contentLoader)
    // theme layouts,includes
    val themeLoaders = themeFolder.toList.flatMap { path =>
      val themeLayoutsLoader = new YamlSkippingFileLoader(path.wrapped)
      themeLayoutsLoader.setPrefix("_layouts")
      themeLayoutsLoader.setSuffix(".peb")
      val themeIncludesLoader = new YamlSkippingFileLoader(path.wrapped)
      themeIncludesLoader.setPrefix("_includes")
      themeIncludesLoader.setSuffix(".peb")
      List(themeLayoutsLoader, themeIncludesLoader)
    }
    val loader = new DelegatingLoader(
      (rootLoaders ++ themeLoaders).asJava
    )
    new PebbleEngine.Builder()
      .loader(loader)
      .registerExtensionCustomizer(e => FlatmarkExtensionCustomizer(e))
      .extension(I18nExtension(flatmarkClassLoader))
      .autoEscaping(false)
      .build()
  }

  def render(templateName: String, context: ju.Map[String, Object], locale: Locale): String = {
    logger.debug(s"Rendering '${templateName}'")
    val compiledTemplate = engine.getTemplate(templateName)
    val writer = new StringWriter()
    compiledTemplate.evaluate(writer, context, locale)
    writer.toString
  }

  def renderFromString(
      templateName: String,
      templateValue: String,
      context: ju.Map[String, Object],
      locale: Locale
  ): String = {
    logger.debug(s"Rendering '${templateName}'")
    val compiledTemplate = engine.getLiteralTemplate(templateValue)
    val writer = new StringWriter()
    compiledTemplate.evaluate(writer, context, locale)
    writer.toString
  }
}
