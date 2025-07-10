package ba.sake.flatmark.ssr

import ba.sake.sharaf.undertow.UndertowSharafServer
import ba.sake.sharaf.SharafHandler
import ba.sake.sharaf.utils.NetworkUtils

class MathRendererSuite extends munit.FunSuite {

  var server: UndertowSharafServer = null
  var webDriverHolder: WebDriverHolder = WebDriverHolder()
  val port = NetworkUtils.getFreePort()

  override def beforeAll(): Unit =
    server = UndertowSharafServer("localhost", port, routes)
    server.start()

  override def afterAll(): Unit =
    server.stop()
    webDriverHolder.quit()

  test("highlight math expression") {
    val ssr = FlatmarkSsr(s"http://localhost:${port}", webDriverHolder)
    val res = ssr.renderMath("""x = 5""")
    assertEquals(
      res,
      """<span class="katex"><span class="katex-mathml"><math xmlns="http://www.w3.org/1998/Math/MathML"><semantics><mrow><mi>x</mi><mo>=</mo><mn>5</mn></mrow><annotation encoding="application/x-tex">            x = 5
        </annotation></semantics></math></span><span class="katex-html" aria-hidden="true"><span class="base"><span class="strut" style="height: 0.4306em;"></span><span class="mord mathnormal">x</span><span class="mspace" style="margin-right: 0.2778em;"></span><span class="mrel">=</span><span class="mspace" style="margin-right: 0.2778em;"></span></span><span class="base"><span class="strut" style="height: 0.6444em;"></span><span class="mord">5</span></span></span></span>"""
    )
  }
}
