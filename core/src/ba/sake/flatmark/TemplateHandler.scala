package ba.sake.flatmark

import java.nio.file.Path
import scala.jdk.CollectionConverters.*
import com.github.jknack.handlebars.{EscapingStrategy, Handlebars}
import com.github.jknack.handlebars.io.FileTemplateLoader

class FlatmarkTemplateHandler(templatesFolder: Path) {
  
  private val templateLoader = FileTemplateLoader(templatesFolder.toFile)
  templateLoader.setSuffix("")
  private val handlebars = Handlebars(templateLoader).`with`(EscapingStrategy.NOOP)
  
  def render(templateValue: String, variables: Map[String, String]): String = {
    val template = handlebars.compileInline(templateValue)
    template.apply(variables.asJava)
  }
}
