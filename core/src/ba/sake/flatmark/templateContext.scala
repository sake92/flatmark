package ba.sake.flatmark

import java.util as ju
import scala.collection.immutable.ListMap
import scala.jdk.CollectionConverters.*

case class TemplateContext(
    site: SiteContext,
    page: PageContext,
    paginator: Option[PaginatorContext] = None
) {
  def toJavaContext: ju.Map[String, Object] =
    Map(
      "site" -> site.toJavaContext,
      "page" -> page.toJavaContext,
      "paginator" -> paginator.map(_.toJavaContext).getOrElse(ju.Collections.emptyMap())
    ).asJava
}

case class SiteContext(
    name: String,
    description: String,
    baseUrl: Option[String],
    langs: Seq[LanguageContext],
    search: SearchContext,
    categories: ListMap[String, CategoryContext],
    tags: ListMap[String, TagContext],
    codeHighlight: CodeHighlightContext,
    mathHighlight: MathHighlightContext,
    data: ju.Map[String, Object]
) {
  def toJavaContext: ju.Map[String, Object] =
    Map(
      "name" -> name,
      "description" -> description,
      "base_url" -> baseUrl.orNull, // Use null if baseUrl is None
      "langs" -> langs.map(_.toJavaContext).asJava,
      "search" -> search.toJavaContext,
      "categories" -> categories.map { case (key, value) => key -> value.toJavaContext }.asJava,
      "tags" -> tags.map { case (key, value) => key -> value.toJavaContext }.asJava,
      "code_highlight" -> codeHighlight.toJavaContext,
      "math_highlight" -> mathHighlight.toJavaContext,
      "data" -> data
    ).asJava
}

case class LanguageContext(
    code: String,
    label: String,
    url: String
) {
  def toJavaContext: ju.Map[String, Object] =
    Map(
      "code" -> code,
      "label" -> label,
      "url" -> url
    ).asJava
}

case class SearchContext(
    enabled: Boolean
) {
  def toJavaContext: ju.Map[String, Object] =
    Map("enabled" -> Boolean.box(enabled)).asJava
}

case class CodeHighlightContext(
    enabled: Boolean
) {
  def toJavaContext: ju.Map[String, Object] =
    Map("enabled" -> Boolean.box(enabled)).asJava
}

case class MathHighlightContext(
    enabled: Boolean = true
) {
  def toJavaContext: ju.Map[String, Object] =
    Map("enabled" -> Boolean.box(enabled)).asJava
}

case class CategoryContext(
    label: String,
    description: String,
    items: Seq[PageContext]
) {
  def toJavaContext: ju.Map[String, Object] =
    Map(
      "label" -> label,
      "description" -> description,
      "items" -> items.map(_.toJavaContext).asJava
    ).asJava
}

case class TagContext(
    label: String,
    description: String
) {
  def toJavaContext: ju.Map[String, Object] =
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
    text: String, // Raw text content, useful for search indexing
    lang: LanguageContext,
    publishDate: Option[java.time.ZonedDateTime],
    rootRelPath: os.RelPath,
    url: String,
    themeProps: Map[String, Any],
    toc: Seq[TocItemContext]
    // TODO summary: String = "", // Optional summary field
) {
  def toJavaContext: ju.Map[String, Object] =
    Map(
      "layout" -> layout,
      "title" -> title,
      "description" -> description,
      "content" -> content,
      "lang" -> lang.toJavaContext,
      "publish_date" -> publishDate.orNull,
      "rootRelPath" -> rootRelPath.toString,
      "url" -> url,
      "theme_props" -> themeProps.asJava,
      "toc" -> toc.map(_.toJavaContext).asJava
    ).asJava

  override def toString: String =
    s"PageContext(layout=$layout, title=$title, description=$description, content=${content.take(20)}..., url=$url)"
}

case class PaginatorContext(
    enabled: Boolean,
    currentPage: Int,
    items: Seq[PageContext],
    totalItems: Int,
    pageSize: Int,
    getUrl: Int => String
) {
  private val totalPages =
    if pageSize == 0 then 1 // Avoid division by zero
    else (totalItems - 1) / pageSize + 1 // Calculate total pages based on total items and page size
  private val hasNext: Boolean = currentPage < totalPages
  private val hasPrev: Boolean = currentPage > 1

  def toJavaContext: ju.Map[String, Object] =
    Map(
      "enabled" -> Boolean.box(enabled),
      "items" -> items.map(_.toJavaContext).asJava,
      "per_page" -> Integer.valueOf(pageSize),
      "total_items" -> Integer.valueOf(totalItems),
      "total_pages" -> Integer.valueOf(totalPages),
      "current" -> Integer.valueOf(currentPage),
      "prev" -> Integer.valueOf(currentPage - 1),
      "next" -> Integer.valueOf(currentPage + 1),
      "has_prev" -> Boolean.box(hasPrev),
      "has_next" -> Boolean.box(hasNext),
      "prev_url" -> getUrl(currentPage - 1),
      "next_url" -> getUrl(currentPage + 1)
    ).asJava
}

case class TocItemContext(
    level: Int,
    title: String,
    url: String,
    children: Seq[TocItemContext]
) {
  def toJavaContext: ju.Map[String, Object] =
    Map(
      "level" -> Integer.valueOf(level),
      "title" -> title,
      "url" -> url,
      "children" -> children.map(_.toJavaContext).asJava
    ).asJava

  override def toString: String =
    ("  " * level) + s"<h${level}>${title}\n" + children.mkString
}
