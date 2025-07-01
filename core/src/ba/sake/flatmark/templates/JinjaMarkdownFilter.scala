package ba.sake.flatmark.templates

import com.hubspot.jinjava.interpret.JinjavaInterpreter
import com.hubspot.jinjava.lib.filter.Filter
import ba.sake.flatmark.markdown.FlatmarkMarkdownRenderer

class JinjaMarkdownFilter(markdownRenderer: FlatmarkMarkdownRenderer) extends Filter {

  override def getName: String = "markdown"
  
  override def filter(variable: AnyRef, interpreter: JinjavaInterpreter, args: String*): AnyRef = {
    val markdownSource = variable.toString
    markdownRenderer.renderMarkdown(markdownSource)
  }

  override def preserveSafeString(): Boolean = false

}
