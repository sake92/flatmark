package ba.sake.flatmark


import java.io.{StringReader, StringWriter}
import scala.jdk.CollectionConverters.*
import com.github.mustachejava.DefaultMustacheFactory

class FlatmarkTemplateHandler {

  private val mf = new DefaultMustacheFactory()
  
  def render(templateName: String, templateValue: String, variables: Map[String, String]): String = {
    val template = mf.compile(new StringReader(templateValue), templateName)
    val sw = new StringWriter()
    template.execute(sw, variables.asJava).flush()
    sw.toString
  }
}
