package ba.sake.flatmark

import java.{util, util as ju}
import org.commonmark.node.*
import org.commonmark.parser.Parser
import org.commonmark.renderer.NodeRenderer
import org.commonmark.renderer.html.{HtmlNodeRendererContext, HtmlNodeRendererFactory, HtmlRenderer}
import org.commonmark.renderer.html.HtmlRenderer.HtmlRendererExtension
import org.commonmark.ext.autolink.AutolinkExtension
import org.commonmark.ext.footnotes.FootnotesExtension
import org.commonmark.ext.gfm.strikethrough.StrikethroughExtension
import org.commonmark.ext.gfm.tables.TablesExtension
import org.commonmark.ext.heading.anchor.HeadingAnchorExtension
import org.commonmark.ext.image.attributes.ImageAttributesExtension
import org.commonmark.ext.ins.InsExtension
import org.commonmark.ext.task.list.items.TaskListItemsExtension

class FlatmarkMarkdownRenderer(nodejsInterop: NodejsInterop) {

  private val extensions = ju.Arrays.asList(
    TablesExtension.create(),
    StrikethroughExtension.create(),
    AutolinkExtension.create,
    HeadingAnchorExtension.create(),
    FootnotesExtension.create(),
    InsExtension.create(),
    ImageAttributesExtension.create(),
    TaskListItemsExtension.create(),
    FlatmarkStaticCodeRendererExtension(nodejsInterop)
  )
  private val parser = Parser.builder.extensions(extensions).build
  private val renderer = HtmlRenderer.builder.extensions(extensions).build

  def renderMarkdown(markdownSource: String): String =
    val document = parser.parse(markdownSource)
    renderer.render(document)
}

class FlatmarkStaticCodeRendererExtension(nodejsInterop: NodejsInterop) extends HtmlRendererExtension {

  override def extend(htmlRendererBuilder: HtmlRenderer.Builder): Unit =
    htmlRendererBuilder.nodeRendererFactory(new HtmlNodeRendererFactory() {
      def create(context: HtmlNodeRendererContext) =
        new FlatmarkStaticCodeNodeRenderer(context, nodejsInterop)
    })

}

class FlatmarkStaticCodeNodeRenderer(context: HtmlNodeRendererContext, nodejsInterop: NodejsInterop)
    extends NodeRenderer {

  override def getNodeTypes: util.Set[Class[? <: Node]] = ju.Set.of(classOf[FencedCodeBlock]);

  override def render(node: Node): Unit = {
    val html = context.getWriter
    val codeBlock = node.asInstanceOf[FencedCodeBlock]
    val codeLang = codeBlock.getInfo // e.g. scala
    val res =
      if codeLang == "math" then nodejsInterop.highlightMath(codeBlock.getLiteral)
      else if codeLang == "diagram:graphviz" then nodejsInterop.renderGraphviz(codeBlock.getLiteral)
      else nodejsInterop.highlightCode(codeBlock.getLiteral, Some(codeLang))
    html.raw(res)
  }
}
