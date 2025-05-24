package ba.sake.flatmark

import java.util as ju
import com.vladsch.flexmark.ast.FencedCodeBlock
import com.vladsch.flexmark.html.renderer.{DelegatingNodeRendererFactory, NodeRenderer, NodeRenderingHandler}
import com.vladsch.flexmark.util.data.DataHolder
import com.vladsch.flexmark.html.HtmlRenderer
import com.vladsch.flexmark.html.HtmlRenderer.HtmlRendererExtension
import com.vladsch.flexmark.util.data.MutableDataHolder
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.data.MutableDataSet;
import com.vladsch.flexmark.ext.tables.TablesExtension
import com.vladsch.flexmark.ext.gfm.strikethrough.StrikethroughExtension

object MarkdownRenderer {

  private val options = new MutableDataSet()
  options.set(
    Parser.EXTENSIONS,
    ju.Arrays.asList(
      TablesExtension.create(),
      StrikethroughExtension.create(),
      HepekStaticCodeRendererExtension("themeName")
    )
  )
  private val parser = Parser.builder(options).build()
  private val renderer = HtmlRenderer.builder(options).build()

  def renderMarkdown(markdownSource: String): String =
    val document = parser.parse(markdownSource)
    renderer.render(document)

}

class HepekStaticCodeRendererExtension(themeName: String) extends HtmlRendererExtension {

  override def rendererOptions(options: MutableDataHolder): Unit = {}

  override def extend(htmlRendererBuilder: HtmlRenderer.Builder, rendererType: String): Unit =
    htmlRendererBuilder.nodeRendererFactory(new HepekStaticCodeNodeRenderer.Factory(themeName))
}

// TODO support math CodeBlock too with `$ MY_FORMULA_X_Y $`
class HepekStaticCodeNodeRenderer(private var options: DataHolder, themeName: String) extends NodeRenderer {

  override def getNodeRenderingHandlers: ju.Set[NodeRenderingHandler[?]] = {
    val set = new ju.HashSet[NodeRenderingHandler[?]]()
    set.add(
      new NodeRenderingHandler(
        classOf[FencedCodeBlock],
        (node, context, html) => {
          val codeLang = node.getInfo.toString // e.g. scala
          val codeBlockText = node.getChars
          val codeBlock =
            codeBlockText.toString.linesIterator.toSeq.tail.dropRight(1).mkString("\n")

          val res =
            if codeLang == "math" then NodejsInterop.highlightMath(codeBlock)
            else if codeLang == "diagram:graphviz" then NodejsInterop.renderGraphviz(codeBlock)
            else NodejsInterop.highlightCode(codeBlock, Some(codeLang))

          html.append(res)
        }
      )
    )
    set
  }

}

object HepekStaticCodeNodeRenderer {

  class Factory(themeName: String) extends DelegatingNodeRendererFactory {
    def apply(options: DataHolder) = new HepekStaticCodeNodeRenderer(options, themeName)

    override def getDelegates: ju.Set[Class[?]] = null
  }
}
