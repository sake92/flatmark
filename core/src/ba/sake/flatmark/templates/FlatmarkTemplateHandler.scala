package ba.sake.flatmark.templates

import ba.sake.flatmark.markdown.FlatmarkMarkdownRenderer

import java.util as ju
import java.util.Locale
import org.slf4j.LoggerFactory
import com.hubspot.jinjava.Jinjava
import com.hubspot.jinjava.loader.{CascadingResourceLocator, FileLocator}
import ba.sake.flatmark.{FlatmarkException, FrontMatterUtils}

class FlatmarkTemplateHandler(flatmarkClassLoader: ClassLoader, 
                              siteRootFolder: os.Path, themeFolder: Option[os.Path],
                             markdownRenderer: FlatmarkMarkdownRenderer) {

  private val logger = LoggerFactory.getLogger(getClass.getName)

  private val resourceFolders = Seq(
    siteRootFolder / "_layouts",
    siteRootFolder / "_includes"
  ) ++ themeFolder.toSeq.flatMap { tf =>
    Seq(
      tf / "_layouts",
      tf / "_includes"
    )
  }

  private val jinjava = new Jinjava()
  jinjava.setResourceLocator(
    new CascadingResourceLocator(
      resourceFolders.filter(os.exists).map(_.wrapped.toFile).map(new FileLocator(_))*
    )
  )
  jinjava.getGlobalContext.registerFilter(
    JinjaMarkdownFilter(markdownRenderer)
  )

  private val layoutLocations =
    Seq(siteRootFolder / "content", siteRootFolder / "_layouts") ++
      themeFolder.toSeq.map(_ / "_layouts")

  def render(templateName: String, context: ju.Map[String, Object], locale: Locale): String = {
    logger.debug(s"Rendering '${templateName}' with context: ${context}")
    val templatePath = locally {
      layoutLocations.map(_ / os.RelPath(templateName)).find(os.exists).getOrElse {
        throw FlatmarkException(s"Template '$templateName' not found in content/ or _layouts/ or theme _layouts/.")
      }
    }
    val rawTemplate = os.read(templatePath)
    val (_, template) = FrontMatterUtils.extract(rawTemplate)
    jinjava.render(template, context)
  }

}
