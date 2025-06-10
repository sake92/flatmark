package ba.sake.flatmark.templates

import java.util as ju
import scala.jdk.CollectionConverters.*
import io.pebbletemplates.pebble.extension.i18n.i18nFunction
import io.pebbletemplates.pebble.extension.{Extension, ExtensionCustomizer}
import io.pebbletemplates.pebble.tokenParser.TokenParser

class FlatmarkExtensionCustomizer(ext: Extension) extends ExtensionCustomizer(ext) {

  override def getTokenParsers: ju.List[TokenParser] =
    val tokenParsers = Option(super.getTokenParsers).map(_.asScala.toSeq).getOrElse(Seq.empty)
    tokenParsers.filterNot(_.isInstanceOf[i18nFunction]).asJava

}
