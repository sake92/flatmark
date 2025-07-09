package ba.sake.flatmark.ssr

class FlatmarkSsr(
    ssrServerUrl: String,
    webDriverHolder: WebDriverHolder
) {
  private val codeSsr = FlatmarkCodeHighlighter(ssrServerUrl, webDriverHolder)
  private val mathSsr = FlatmarkMathRenderer(ssrServerUrl, webDriverHolder)
  private val graphvizSsr = FlatmarkGraphvizRenderer(ssrServerUrl, webDriverHolder)
  private val mermaidSsr = FlatmarkMermaidRenderer(ssrServerUrl, webDriverHolder)

  def highlight(codeStr: String, codeLang: Option[String] = None): String =
    codeSsr.highlight(codeStr, codeLang)

  def renderMath(mathStr: String): String =
    mathSsr.render(mathStr)

  def renderGraphviz(dotStr: String, engine: String = "dot"): String =
    graphvizSsr.render(dotStr, engine)

  def renderMermaid(source: String): String =
    mermaidSsr.render(source)
}
