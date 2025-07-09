package ba.sake.flatmark.ssr

import ba.sake.sharaf.undertow.UndertowSharafServer
import ba.sake.sharaf.SharafHandler
import ba.sake.sharaf.utils.NetworkUtils
import org.jsoup.Jsoup

class CodeHighlighterSuite extends munit.FunSuite {

  var server: UndertowSharafServer = null
  var webDriverHolder: WebDriverHolder = WebDriverHolder()
  val port = NetworkUtils.getFreePort()

  override def beforeAll(): Unit =
    server = UndertowSharafServer("localhost", port, routes)
    server.start()

  override def afterAll(): Unit =
    server.stop()
    webDriverHolder.close()

  test("highlight scala code") {
    val ssr = FlatmarkSsr(s"http://localhost:${port}", webDriverHolder)
    val res = ssr.highlight(
      """|object HelloWorld {
         |  def main(args: Array[String]): Unit = {
         |    println("Hello, world!")
         |  }
         |}""".stripMargin,
      Some("scala")
    )
    val resHtml = Jsoup.parse(res)
    val expectedHtml = Jsoup.parse(
      """<pre>            <code class="language-scala hljs" data-highlighted="yes"><span class="hljs-class"><span class="hljs-keyword">object</span> <span class="hljs-title">HelloWorld</span> </span>{
  <span class="hljs-function"><span class="hljs-keyword">def</span> <span class="hljs-title">main</span></span>(args: <span class="hljs-type">Array</span>[<span class="hljs-type">String</span>]): <span class="hljs-type">Unit</span> = {
    println(<span class="hljs-string">"Hello, world!"</span>)
  }
}</code>
          </pre>"""
    )
    assertEquals(resHtml.html, expectedHtml.html)
  }

  // these are sensitive, because of HMTL encoding etc
  test("highlight HTML code") {
    val ssr = FlatmarkSsr(s"http://localhost:${port}", webDriverHolder)
    val res = ssr.highlight(
      """|<!DOCTYPE html>
         |<html>
         |<head>
         |<title>Page Title</title>
         |</head>
         |<body>
         |
         |<h1>This is a Heading</h1>
         |<p>This is a paragraph.</p>
         |
         |</body>
         |</html>
         |""".stripMargin,
      Some("html")
    )
    val resHtml = Jsoup.parse(res)
    val expectedHtml = Jsoup.parse(
      """<pre>            <code class="language-html hljs language-xml" data-highlighted="yes"><span class="hljs-meta">&lt;!DOCTYPE <span class="hljs-keyword">html</span>&gt;</span>
<span class="hljs-tag">&lt;<span class="hljs-name">html</span>&gt;</span>
<span class="hljs-tag">&lt;<span class="hljs-name">head</span>&gt;</span>
<span class="hljs-tag">&lt;<span class="hljs-name">title</span>&gt;</span>Page Title<span class="hljs-tag">&lt;/<span class="hljs-name">title</span>&gt;</span>
<span class="hljs-tag">&lt;/<span class="hljs-name">head</span>&gt;</span>
<span class="hljs-tag">&lt;<span class="hljs-name">body</span>&gt;</span>

<span class="hljs-tag">&lt;<span class="hljs-name">h1</span>&gt;</span>This is a Heading<span class="hljs-tag">&lt;/<span class="hljs-name">h1</span>&gt;</span>
<span class="hljs-tag">&lt;<span class="hljs-name">p</span>&gt;</span>This is a paragraph.<span class="hljs-tag">&lt;/<span class="hljs-name">p</span>&gt;</span>

<span class="hljs-tag">&lt;/<span class="hljs-name">body</span>&gt;</span>
<span class="hljs-tag">&lt;/<span class="hljs-name">html</span>&gt;</span>
</code>
          </pre>"""
    )
    assertEquals(resHtml.html, expectedHtml.html)
  }

}
