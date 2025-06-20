package ba.sake.flatmark

import scala.jdk.CollectionConverters.*

case class TemplateContext(
    site: SiteContext,
    page: PageContext,
    paginator: Option[PaginatorContext] = None
) {
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
    langs: Seq[LanguageContext],
    categories: Map[String, CategoryContext],
    tags: Map[String, TagContext]
) {
  def toPebbleContext: java.util.Map[String, Object] = {
    Map(
      "name" -> name,
      "description" -> description,
      "langs" -> langs.map(_.toPebbleContext).asJava,
      "categories" -> categories.map { case (key, value) => key -> value.toPebbleContext }.asJava,
      "tags" -> tags.map { case (key, value) => key -> value.toPebbleContext }.asJava
    ).asJava
  }
}

case class LanguageContext(
    code: String,
    label: String,
    url: String
) {
  def toPebbleContext: java.util.Map[String, Object] =
    Map(
      "code" -> code,
      "label" -> label,
      "url" -> url
    ).asJava
}

case class CategoryContext(
    label: String,
    description: String = ""
) {
  def toPebbleContext: java.util.Map[String, Object] =
    Map(
      "label" -> label,
      "description" -> description
    ).asJava
}

case class TagContext(
    label: String,
    description: String = ""
) {
  def toPebbleContext: java.util.Map[String, Object] =
    Map(
      "label" -> label,
      "description" -> description
    ).asJava
}

case class PageContext(
    layout: String,
    title: String,
    description: String,
    content: String,
    lang: LanguageContext,
    publishDate: Option[java.time.ZonedDateTime],
    rootRelPath: os.RelPath
    // TODO summary: String = "", // Optional summary field
    // TODO toc: Tree[PageLinkContext] = Tree.empty // Optional table of contents, only link and title for now
) {
  def toPebbleContext: java.util.Map[String, Object] = {
    Map(
      "layout" -> layout,
      "title" -> title,
      "description" -> description,
      "content" -> content,
      "lang" -> lang.toPebbleContext,
      "publish_date" -> publishDate.orNull,
      "url" -> s"/${rootRelPath.segments.mkString("/")}"
    ).asJava
  }

  override def toString: String =
    s"PageContext(layout=$layout, title=$title, description=$description, content=${content.take(20)}..., rootRelPath=$rootRelPath)"
}

case class PaginatorContext(
    currentPage: Int,
    items: Seq[PageContext],
    totalItems: Int,
    pageSize: Int,
    rootRelPath: Int => os.RelPath
) {
  private val totalPages =
    if pageSize == 0 then 1 // Avoid division by zero
    else (totalItems - 1) / pageSize + 1 // Calculate total pages based on total items and page size
  private val hasNext: Boolean = currentPage < totalPages
  private val hasPrev: Boolean = currentPage > 1

  def toPebbleContext: java.util.Map[String, Object] = {
    Map(
      "items" -> items.map(_.toPebbleContext).asJava,
      "perPage" -> Integer.valueOf(pageSize),
      "totalItems" -> Integer.valueOf(totalItems),
      "totalPages" -> Integer.valueOf(totalPages),
      "current" -> Integer.valueOf(currentPage),
      "prev" -> Integer.valueOf(currentPage - 1),
      "next" -> Integer.valueOf(currentPage + 1),
      "hasPrev" -> Boolean.box(hasPrev),
      "hasNext" -> Boolean.box(hasNext),
      "prevUrl" -> s"/${rootRelPath(currentPage - 1).segments.mkString("/")}",
      "nextUrl" -> s"/${rootRelPath(currentPage + 1).segments.mkString("/")}"
    ).asJava
  }
}
