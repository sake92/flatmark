package ba.sake.flatmark

import ba.sake.flatmark.ssr.FlatmarkSsr

class CachingFlatmarkSsr(
    flatmarkSsr: FlatmarkSsr,
    fileCache: FileCache
) {

  def highlight(codeStr: String, codeLang: Option[String] = None): String =
    fileCache.cached("highlightjs", codeStr, codeLang.getOrElse("plaintext")) {
      flatmarkSsr.highlight(codeStr, codeLang)
    }

  def renderMath(mathStr: String): String =
    fileCache.cached("katex", mathStr) {
      flatmarkSsr.renderMath(mathStr)
    }

  def renderGraphviz(dotStr: String, engine: String = "dot"): String =
    fileCache.cached("graphviz", dotStr, engine) {
      flatmarkSsr.renderGraphviz(dotStr, engine)
    }

  def renderMermaid(source: String): String =
    fileCache.cached("mermaid", source) {
      flatmarkSsr.renderMermaid(source)
    }
}
