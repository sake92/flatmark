package ba.sake.flatmark.ssr

import scalatags.Text.all.*
import ba.sake.sharaf.*
import ba.sake.querson.*

val routes = Routes {
  case GET -> Path("ssr", "highlightjs") =>
    case class QP(code: String, lang: String) derives QueryStringRW
    val qp = Request.current.queryParams[QP]
    Response.withBody(
      htmlPage(
        div(id := "result")(
          pre(
            code(cls := s"language-${qp.lang}")(
              qp.code
            )
          )
        ),
        script(`type` := "module")(
          raw("""
            import { hljs } from '/highlightjs.js';
            hljs.highlightAll();
            window.renderFinished = true;
          """)
        )
      )
    )
  case GET -> Path("ssr", "katex") =>
    case class QP(source: String) derives QueryStringRW
    val qp = Request.current.queryParams[QP]
    Response.withBody(
      htmlPage(
        pre(id := "input")(
          qp.source
        ),
        div(id := "result")(),
        script(`type` := "module")(
          raw(s"""
            import katex from '/katex.min.js';
            const input = document.getElementById('input');
            const result = document.getElementById('result');
            katex.render(input.innerText, result, { throwOnError: true });
            window.renderFinished = true;
          """)
        )
      )
    )
  case GET -> Path("ssr", "graphviz") =>
    case class QP(source: String, engine: String) derives QueryStringRW
    val qp = Request.current.queryParams[QP]
    Response.withBody(
      htmlPage(
        div(id := "result")(),
        script(`type` := "module")(
          raw(s"""
            import { Graphviz } from '/graphviz.js';
            const graphviz = await Graphviz.load();
            const svg = graphviz.layout(String.raw`${qp.source}`, 'svg', '${qp.engine}');
            document.getElementById('result').innerHTML = svg;
            window.renderFinished = true;
          """)
        )
      )
    )
  case GET -> Path("ssr", "mermaid") =>
    case class QP(source: String) derives QueryStringRW
    val qp = Request.current.queryParams[QP]
    Response.withBody(
      htmlPage(
        div(id := "result")(
          pre(cls := "mermaid")(
            qp.source
          )
        ),
        script(`type` := "module")(
          raw("""
            import { mermaid } from '/mermaidjs.js';
            mermaid.run({
              querySelector: '.mermaid',
              postRenderCallback: (id) => {
                console.log(id);
                window.renderFinished = true;
              }
            });
          """)
        )
      )
    )
}

def htmlPage(bodyContent: Frag*) = doctype("html")(
  html(lang := "en")(
    head(
      meta(charset := "UTF-8"),
      meta(name := "viewport", content := "width=device-width, initial-scale=1.0")
    ),
    body(
      bodyContent
    )
  )
)
