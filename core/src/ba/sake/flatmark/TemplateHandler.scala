package ba.sake.flatmark

import scala.jdk.CollectionConverters.*
import com.github.jknack.handlebars.Handlebars
import com.github.jknack.handlebars.io.FileTemplateLoader

import java.nio.file.Paths

class TemplateHandler {

  def render(): String = {
    val templateLoader = new FileTemplateLoader(Paths.get("site1").toFile)
    templateLoader.setSuffix(".md") // Set the suffix for markdown files
    val handlebars = new Handlebars(templateLoader)

    val template = handlebars.compile("index")

    template.apply(Map(
      "title" -> "Flatmark Example",
      "content" -> "This is a sample content rendered from a Handlebars template."
    ).asJava)
  }
}
