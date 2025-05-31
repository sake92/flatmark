package ba.sake.flatmark

import java.io.{StringReader, StringWriter}
import scala.jdk.CollectionConverters.*
import com.github.mustachejava.DefaultMustacheFactory
import org.virtuslab.yaml.*

class FlatmarkTemplateHandler {

  private val mf = new DefaultMustacheFactory()

  def render[T](templateName: String, templateValue: String, templateData: T)(using codec: YamlCodec[T]): String = {
    val template = mf.compile(new StringReader(templateValue), templateName)
    val sw = new StringWriter()
    val templateDataObject = yamlNodeToObject(codec.asNode(templateData))
    template.execute(sw, templateDataObject).flush()
    sw.toString
  }

  private def yamlNodeToObject(node: Node): Object = node match {
    case sn: Node.ScalarNode   => sn.value
    case sn: Node.SequenceNode => sn.nodes.map(yamlNodeToObject).asJava
    case mn: Node.MappingNode =>
      mn.mappings.map { case (key, value) =>
        yamlNodeToObject(key) -> yamlNodeToObject(value)
      }.asJava
  }
}
