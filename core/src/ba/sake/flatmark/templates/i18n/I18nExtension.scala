package ba.sake.flatmark.templates.i18n

import io.pebbletemplates.pebble.extension.AbstractExtension
import io.pebbletemplates.pebble.extension.Function
import java.util

class I18nExtension(flatmarkClassLoader: ClassLoader) extends AbstractExtension {
  override def getFunctions: util.Map[String, Function] = {
    val functions = new util.HashMap[String, Function]
    functions.put("i18n", new i18nFunction(flatmarkClassLoader))
    functions
  }
}