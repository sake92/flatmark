package ba.sake.flatmark.ssr

import play.twirl.api.HtmlFormat
import ba.sake.sharaf.{*, given}
import ba.sake.querson.*

val routes = Routes {
  case GET -> Path("ssr", "highlightjs") =>
    case class QP(code: String, lang: String) derives QueryStringRW
    val qp = Request.current.queryParams[QP]
    Response.withBody(
      htmlPage(
        html"""
        <div id="result">
          <pre>
            <code class="language-${HtmlFormat.raw(qp.lang)}">${qp.code}</code>
          </pre>
        </div>
        <script type="module">
            import { hljs } from '/highlightjs.js';
            hljs.highlightAll();
            window.renderFinished = true;
        </script>
        """
      )
    )
  case GET -> Path("ssr", "katex") =>
    case class QP(source: String) derives QueryStringRW
    val qp = Request.current.queryParams[QP]
    Response.withBody(
      htmlPage(
        html"""
        <pre id="input">
            ${qp.source}
        </pre>
        <div id="result"></div>
        <script type="module">
            import katex from '/katex.min.js';
            const input = document.getElementById('input');
            const result = document.getElementById('result');
            katex.render(input.innerText, result, { throwOnError: true });
            window.renderFinished = true;
        </script>
        """
      )
    )
  case GET -> Path("ssr", "graphviz") =>
    case class QP(source: String, engine: String) derives QueryStringRW
    val qp = Request.current.queryParams[QP]
    Response.withBody(
      htmlPage(
        html"""
        <div id="result"></div>
        <script type="module">
            import { Graphviz } from '/graphviz.js';
            const graphviz = await Graphviz.load();
            const svg = graphviz.layout(String.raw`${HtmlFormat.raw(qp.source)}`, 'svg', '${HtmlFormat.raw(qp.engine)}');
            document.getElementById('result').innerHTML = svg;
            window.renderFinished = true;
        </script>
        """
      )
    )
  case GET -> Path("ssr", "mermaid") =>
    case class QP(source: String) derives QueryStringRW
    val qp = Request.current.queryParams[QP]
    Response.withBody(
      htmlPage(
        html"""
        <div id="result">
          <pre class="mermaid">
            ${qp.source}
          </pre>
        </div>
        <script type="module">
            import { mermaid } from '/mermaidjs.js';
            mermaid.run({
                querySelector: '.mermaid',
                postRenderCallback: (id) => {
                    window.renderFinished = true;
                }
            });
        </script>
        """
      )
    )
}

def htmlPage(bodyContent: Html*) =
  html"""
    <!DOCTYPE html>
    <html lang="en">
    <head>
    <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
    </head>
    <body>
        ${bodyContent}
    </body>
    </html>
  """
