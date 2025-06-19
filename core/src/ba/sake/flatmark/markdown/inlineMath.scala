package ba.sake.flatmark.markdown

import ba.sake.flatmark.math.FlatmarkMathRenderer

import scala.jdk.CollectionConverters.*
import org.commonmark.node.{CustomNode, Delimited}
import org.commonmark.ext.ins.Ins
import org.commonmark.node.Node
import org.commonmark.node.Nodes
import org.commonmark.node.SourceSpans
import org.commonmark.node.Text
import org.commonmark.parser.Parser
import org.commonmark.parser.delimiter.DelimiterProcessor
import org.commonmark.parser.delimiter.DelimiterRun
import org.commonmark.renderer.NodeRenderer
import org.commonmark.renderer.html.{HtmlNodeRendererContext, HtmlNodeRendererFactory, HtmlRenderer}
import org.commonmark.renderer.markdown.MarkdownRenderer
import org.commonmark.renderer.text.TextContentRenderer
import org.commonmark.node.Node
import org.commonmark.renderer.html.HtmlNodeRendererContext
import org.commonmark.renderer.html.HtmlWriter

import java.util

object InlineMathExtension {
  def create(mathRenderer: FlatmarkMathRenderer): InlineMathExtension = new InlineMathExtension(mathRenderer)
}

class InlineMathExtension(mathRenderer: FlatmarkMathRenderer) extends Parser.ParserExtension, HtmlRenderer.HtmlRendererExtension {
  override def extend(parserBuilder: Parser.Builder): Unit =
    parserBuilder.customDelimiterProcessor(new InlineMathDelimiterProcessor())

  override def extend(rendererBuilder: HtmlRenderer.Builder): Unit =
    rendererBuilder.nodeRendererFactory((context: HtmlNodeRendererContext) => new InlineMathHtmlNodeRenderer(context, mathRenderer))
}

class InlineMathNode extends CustomNode with Delimited {
  private val Delimiter = "$"
  override def getOpeningDelimiter: String = Delimiter
  override def getClosingDelimiter: String = Delimiter
}

class InlineMathDelimiterProcessor extends DelimiterProcessor {
  override def getOpeningCharacter = '$'
  override def getClosingCharacter = '$'

  override def getMinLength = 1

  override def process(openingRun: DelimiterRun, closingRun: DelimiterRun): Int =
    if (openingRun.length >= 1 && closingRun.length >= 1) {
      // Use exactly 1 delimiter even if we have more, and don't care about internal openers/closers.
      val opener = openingRun.getOpener
      // Wrap nodes between delimiters in ins.
      val inlineMath = new InlineMathNode
      val sourceSpans = new SourceSpans
      sourceSpans.addAllFrom(openingRun.getOpeners(1))
      for (node <- Nodes.between(opener, closingRun.getCloser).asScala) {
        inlineMath.appendChild(node)
        sourceSpans.addAll(node.getSourceSpans)
      }
      sourceSpans.addAllFrom(closingRun.getClosers(1))
      inlineMath.setSourceSpans(sourceSpans.getSourceSpans)
      opener.insertAfter(inlineMath)
      1
    } else {
      0
    }
}


class InlineMathHtmlNodeRenderer(context: HtmlNodeRendererContext, mathRenderer: FlatmarkMathRenderer) extends NodeRenderer {
  private val html = context.getWriter

  override def getNodeTypes: util.Set[Class[? <: Node]] = util.Set.of(classOf[InlineMathNode])

  override def render(node: Node): Unit = {
    val literalText = getLiteralText(node)
    val ssrRendered = mathRenderer.render(literalText)
    html.raw(ssrRendered)
  }

  private def getLiteralText(node: Node) = {
    val sb = new java.lang.StringBuilder
    collectLiteralText(node, sb)
    sb.toString
  }

  private def collectLiteralText(node: Node, sb: java.lang.StringBuilder): Unit = {
    node match {
      case text: Text => sb.append(text.getLiteral)
      case _ => () // do nothing for other node types
    }
    var child = node.getFirstChild
    while (child != null) {
      collectLiteralText(child, sb)
      child = child.getNext
    }
  }

}