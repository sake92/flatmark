package ba.sake.flatmark.ssr

import ba.sake.sharaf.undertow.UndertowSharafServer
import ba.sake.sharaf.SharafHandler
import ba.sake.sharaf.utils.NetworkUtils

class MermaidRendererSuite extends munit.FunSuite {

  var server: UndertowSharafServer = null
  var webDriverHolder: WebDriverHolder = WebDriverHolder()
  val port = NetworkUtils.getFreePort()

  override def beforeAll(): Unit =
    server = UndertowSharafServer("localhost", port, routes)
    server.start()

  override def afterAll(): Unit =
    server.stop()
    webDriverHolder.close()

  test("render mermaid flowchart diagram to HTML") {
    val ssr = FlatmarkSsr(s"http://localhost:${port}", webDriverHolder)
    val source =
      """|pie title pita
         |    "Dogs" : 50
         |    "Cats" : 50
         |""".stripMargin
    val res = ssr.renderMermaid(source).trim
    assert(
      res.startsWith("""<pre class="mermaid" data-processed="true"><svg id="mermaid-""")
    )
    assert(
      res.endsWith(
        """<g transform="translate(225,225)"><circle cx="0" cy="0" r="186" class="pieOuterCircle"></circle><path d="M0,-185A185,185,0,1,1,0,185L0,0Z" fill="#ECECFF" class="pieCircle"></path><path d="M0,185A185,185,0,1,1,0,-185L0,0Z" fill="#ffffde" class="pieCircle"></path><text transform="translate(138.75,3.0808688933348094e-14)" class="slice" style="text-anchor: middle;">50%</text><text transform="translate(-138.75,-1.0624278139522284e-13)" class="slice" style="text-anchor: middle;">50%</text><text x="0" y="-200" class="pieTitleText">pita</text><g class="legend" transform="translate(216,-22)"><rect width="18" height="18" style="fill: rgb(236, 236, 255); stroke: rgb(236, 236, 255);"></rect><text x="22" y="14">Dogs</text></g><g class="legend" transform="translate(216,0)"><rect width="18" height="18" style="fill: rgb(255, 255, 222); stroke: rgb(255, 255, 222);"></rect><text x="22" y="14">Cats</text></g></g></svg></pre>"""
      )
    )
  }
}
