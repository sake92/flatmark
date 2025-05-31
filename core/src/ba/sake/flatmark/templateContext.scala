package ba.sake.flatmark

import scala.jdk.CollectionConverters.*

case class TemplateContext(site: SiteContext, page: PageContext) {
  def toPebbleContext: java.util.Map[String, Object] = {
    Map(
      "site" -> site.toPebbleContext,
      "page" -> page.toPebbleContext
    ).asJava
  }
}

case class SiteContext(
    name: String,
    description: String,
    posts: Seq[PageContext]
) {
  def toPebbleContext: java.util.Map[String, Object] = {
    Map(
      "name" -> name,
      "description" -> description,
      "posts" -> posts.asJava
    ).asJava
  }
}

case class PageContext(
    title: String,
    description: String,
    content: String,
    url: String
) {
  def toPebbleContext: java.util.Map[String, Object] = {
    Map(
      "title" -> title,
      "description" -> description,
      "content" -> content,
      "url" -> url
    ).asJava
  }
}
