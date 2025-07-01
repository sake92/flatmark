package ba.sake.flatmark.templates

import ba.sake.flatmark.markdown.FlatmarkMarkdownRenderer
import com.hubspot.jinjava.interpret.JinjavaInterpreter
import com.hubspot.jinjava.lib.filter.Filter

class JinjaMarkdownFilter(markdownRenderer: FlatmarkMarkdownRenderer) extends Filter {

  override def getName: String = "markdown"
  
  override def filter(variable: AnyRef, interpreter: JinjavaInterpreter, args: String*): AnyRef = {
    val markdownSource = variable.toString
    
    val res = markdownRenderer.renderMarkdown(markdownSource)
    println("*" * 80)
    println(s"Markdownifying: '$markdownSource' .. res = '$res'")
    res
  }

  override def preserveSafeString(): Boolean = false

}
