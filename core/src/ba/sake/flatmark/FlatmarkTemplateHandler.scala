package ba.sake.flatmark

import java.util as ju
import java.io.{StringReader, StringWriter}
import com.github.mustachejava.DefaultMustacheFactory

class FlatmarkTemplateHandler {

  private val mf = new DefaultMustacheFactory()
  
  def render(templateName: String, templateValue: String, templateData: ju.Map[String, Object]): String = {
    val template = mf.compile(new StringReader(templateValue), templateName)
    val sw = new StringWriter()
    template.execute(sw, templateData).flush()
    sw.toString
  }
}
