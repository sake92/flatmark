package ba.sake.flatmark.templates

import java.io.StringWriter
import java.util as ju
import java.util.Locale
import org.slf4j.LoggerFactory
import io.pebbletemplates.pebble.PebbleEngine
import io.pebbletemplates.pebble.loader.DelegatingLoader
import ba.sake.flatmark.templates.i18n.I18nExtension

class FlatmarkTemplateHandler(flatmarkClassLoader: ClassLoader, siteRootFolder: os.Path, themeFolder: os.Path) {

  private val logger = LoggerFactory.getLogger(getClass.getName)

  private val engine = locally {
    // theme layouts,includes
    val themeLayoutsLoader = new YamlSkippingFileLoader(themeFolder.wrapped)
    themeLayoutsLoader.setPrefix("_layouts")
    themeLayoutsLoader.setSuffix(".peb")
    val themeIncludesLoader = new YamlSkippingFileLoader(themeFolder.wrapped)
    themeIncludesLoader.setPrefix("_includes")
    themeIncludesLoader.setSuffix(".peb")
    // local layouts,includes
    val layoutsLoader = new YamlSkippingFileLoader(siteRootFolder.wrapped)
    layoutsLoader.setPrefix("_layouts")
    layoutsLoader.setSuffix(".peb")
    val includesLoader = new YamlSkippingFileLoader(siteRootFolder.wrapped)
    includesLoader.setPrefix("_includes")
    includesLoader.setSuffix(".peb")
    val contentLoader = new YamlSkippingFileLoader(siteRootFolder.wrapped)
    contentLoader.setPrefix("content")
    val loader = new DelegatingLoader(
      ju.Arrays.asList(contentLoader, layoutsLoader, includesLoader, themeLayoutsLoader, themeIncludesLoader)
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

  def renderFromString(templateName: String, templateValue: String, context: ju.Map[String, Object], locale: Locale): String = {
    logger.debug(s"Rendering '${templateName}'")
    val compiledTemplate = engine.getLiteralTemplate(templateValue)
    val writer = new StringWriter()
    compiledTemplate.evaluate(writer, context, locale)
    writer.toString
  }
}
