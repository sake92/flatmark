package ba.sake.flatmark.markdown

import ba.sake.flatmark.codehighlight.FlatmarkCodeHighlighter
import ba.sake.flatmark.diagrams.FlatmarkGraphvizRenderer
import ba.sake.flatmark.diagrams.FlatmarkMermaidRenderer
import ba.sake.flatmark.math.FlatmarkMathRenderer
import org.commonmark.ext.autolink.AutolinkExtension
import org.commonmark.ext.footnotes.FootnotesExtension
import org.commonmark.ext.gfm.strikethrough.StrikethroughExtension
import org.commonmark.ext.gfm.tables.TablesExtension
import org.commonmark.ext.heading.anchor.HeadingAnchorExtension
import org.commonmark.ext.image.attributes.ImageAttributesExtension
import org.commonmark.ext.ins.InsExtension
import org.commonmark.ext.task.list.items.TaskListItemsExtension
import org.commonmark.node.*
import org.commonmark.parser.Parser
import org.commonmark.renderer.NodeRenderer
import org.commonmark.renderer.html.HtmlRenderer.HtmlRendererExtension
import org.commonmark.renderer.html.{HtmlNodeRendererContext, HtmlNodeRendererFactory, HtmlRenderer}

import java.{util, util as ju}

class FlatmarkMarkdownRenderer(
    codeHighlighter: FlatmarkCodeHighlighter,
    graphvizRenderer: FlatmarkGraphvizRenderer,
    mermaidRenderer: FlatmarkMermaidRenderer,
    mathRenderer: FlatmarkMathRenderer
) {

  private val extensions = ju.Arrays.asList(
    TablesExtension.create(),
    StrikethroughExtension.create(),
    AutolinkExtension.create,
    HeadingAnchorExtension.create(),
    FootnotesExtension.create(),
    InsExtension.create(),
    ImageAttributesExtension.create(),
    TaskListItemsExtension.create(),
    FlatmarkStaticCodeRendererExtension(codeHighlighter, graphvizRenderer, mermaidRenderer, mathRenderer)
  )
  private val parser = Parser.builder.extensions(extensions).build
  private val renderer = HtmlRenderer.builder.extensions(extensions).build

  def renderMarkdown(markdownSource: String): String =
    val document = parser.parse(markdownSource)
    renderer.render(document)
}

class FlatmarkStaticCodeRendererExtension(
    codeHighlighter: FlatmarkCodeHighlighter,
    graphvizRenderer: FlatmarkGraphvizRenderer,
    mermaidRenderer: FlatmarkMermaidRenderer,
    mathRenderer: FlatmarkMathRenderer
) extends HtmlRendererExtension {

  override def extend(htmlRendererBuilder: HtmlRenderer.Builder): Unit =
    htmlRendererBuilder.nodeRendererFactory(new HtmlNodeRendererFactory() {
      def create(context: HtmlNodeRendererContext) =
        new FlatmarkStaticCodeNodeRenderer(context, codeHighlighter, graphvizRenderer, mermaidRenderer, mathRenderer)
    })

}

class FlatmarkStaticCodeNodeRenderer(
    context: HtmlNodeRendererContext,
    codeHighlighter: FlatmarkCodeHighlighter,
    graphvizRenderer: FlatmarkGraphvizRenderer,
    mermaidRenderer: FlatmarkMermaidRenderer,
    mathRenderer: FlatmarkMathRenderer
) extends NodeRenderer {

  override def getNodeTypes: util.Set[Class[? <: Node]] = ju.Set.of(classOf[FencedCodeBlock]);

  override def render(node: Node): Unit = {
    val html = context.getWriter
    val codeBlock = node.asInstanceOf[FencedCodeBlock]
    val codeBlockLiteral = codeBlock.getLiteral
    val codeLang = codeBlock.getInfo
    val res =
      if codeLang == "math" then mathRenderer.render(codeBlockLiteral)
      else if codeLang == "diagram:graphviz" then graphvizRenderer.render(codeBlockLiteral)
      else if codeLang == "diagram:mermaid" then mermaidRenderer.render(codeBlockLiteral)
      else codeHighlighter.highlight(codeBlockLiteral, Some(codeLang))
    html.raw(res)
  }
}
