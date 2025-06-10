package ba.sake.flatmark.templates.i18n

import io.pebbletemplates.pebble.extension.Function
import io.pebbletemplates.pebble.template.EvaluationContext
import io.pebbletemplates.pebble.template.PebbleTemplate
import java.text.MessageFormat
import java.util
import java.util.ResourceBundle

/**
 * Pebble is using ResourceBundle.getBundle(basename, locale, new UTF8Control()). 
 * And that in turn uses caller's module.getClassLoader() and NOT Thread.currentThread.getContextClassLoader.
 * So we need to pass the Flatmark classloader to this function.
 * @param flatmarkClassLoader
 */
class i18nFunction(flatmarkClassLoader: ClassLoader) extends Function {
  
  private val argumentNames = new util.ArrayList[String]
  argumentNames.add("bundle")
  argumentNames.add("key")
  argumentNames.add("params")

  override def getArgumentNames: util.List[String] = this.argumentNames

  override def execute(
      args: util.Map[String, AnyRef],
      self: PebbleTemplate,
      context: EvaluationContext,
      lineNumber: Int
  ): AnyRef = {
    val basename = args.get("bundle").asInstanceOf[String]
    val key = args.get("key").asInstanceOf[String]
    val params = args.get("params")
    val locale = context.getLocale
    val bundle = ResourceBundle.getBundle(basename, locale, flatmarkClassLoader)
    val phraseObject = bundle.getObject(key)
    if params != null then params match {
      case list: util.List[_] =>
        return MessageFormat.format(phraseObject.toString, list.toArray)
      case _ => return MessageFormat.format(phraseObject.toString, params)
    }
    phraseObject
  }
}
