package ba.sake.flatmark

import scala.jdk.CollectionConverters.*

case class TemplateContext(site: SiteContext, page: PageContext, paginator: Option[PaginatorContext] = None) {
  def toPebbleContext: java.util.Map[String, Object] = {
    Map(
      "site" -> site.toPebbleContext,
      "page" -> page.toPebbleContext,
      "paginator" -> paginator.map(_.toPebbleContext).getOrElse(java.util.Collections.emptyMap())
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
    layout: String,
    title: String,
    description: String,
    content: String,
    rootRelPath: String
) {
  def toPebbleContext: java.util.Map[String, Object] = {
    Map(
      "layout" -> layout,
      "title" -> title,
      "description" -> description,
      "content" -> content,
      "url" -> rootRelPath
    ).asJava
  }
}
case class PaginatorContext(
    currentPage: Int,
    items: Seq[PageContext],
    totalItems: Int,
    pageSize: Int,
    outputFileRelPath: Int => String
) {
  private val totalPages =
    if pageSize == 0 then 1 // Avoid division by zero
    else (totalItems - 1) / pageSize + 1 // Calculate total pages based on total items and page size
  private val hasNext: Boolean = currentPage < totalPages
  private val hasPrev: Boolean = currentPage > 1

  def toPebbleContext: java.util.Map[String, Object] = {
    Map(
      "items" -> items.map(_.toPebbleContext).asJava,
      "totalItems" -> Integer.valueOf(totalItems),
      "totalPages" -> Integer.valueOf(totalPages),
      //"prevPage" -> Integer.valueOf(currentPage - 1),
      "currentPage" -> Integer.valueOf(currentPage),
      //"nextPage" -> Integer.valueOf(currentPage + 1),
      "hasPrev" -> Boolean.box(hasPrev),
      "hasNext" -> Boolean.box(hasNext),
      "prev" -> outputFileRelPath(currentPage - 1),
      "next" -> outputFileRelPath(currentPage + 1)
    ).asJava
  }
}
