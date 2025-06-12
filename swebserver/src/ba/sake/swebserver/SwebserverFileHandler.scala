package ba.sake.swebserver

import java.io.IOException
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Path}
import scala.util.boundary
import io.undertow.server.{HttpHandler, HttpServerExchange}
import io.undertow.server.handlers.resource.{PathResourceManager, ResourceChangeListener}
import io.undertow.util.Headers


class SwebserverFileHandler(baseFolder: os.Path, address: String, port: Int, next: HttpHandler) extends HttpHandler {

  private val resourceManager = new PathResourceManager(baseFolder.wrapped)

  private val InjectedScript =
    s"""
      |<script>
      |    const ws = new WebSocket("ws://${address}:${port}/ws");
      |
      |    ws.onopen = (event) => {
      |        console.log("[Swebserver] WebSocket connection established.", event);
      |    };
      |
      |    ws.onmessage = (event) => {
      |        console.log("[Swebserver] Message from server:", event.data);
      |        if (event.data === "reload") {
      |            console.log("[Swebserver] Reloading page...");
      |            location.reload();
      |        }
      |    };
      |
      |    ws.onclose = (event) => {
      |        console.warn("[Swebserver] WebSocket connection closed:", event);
      |        // Attempt to reconnect after a short delay
      |        setTimeout(() => {
      |            console.log("[Swebserver] Attempting to reconnect...");
      |            window.location.reload();
      |        }, 1000);
      |    };
      |
      |    ws.onerror = (error) => {
      |        console.error("[Swebserver] WebSocket error:", error);
      |    };
      |</script>
      |""".stripMargin

  override def handleRequest(exchange: HttpServerExchange): Unit = boundary {
    val requestPath = exchange.getRelativePath
    var resource = resourceManager.getResource(requestPath)
    if resource == null then next.handleRequest(exchange)
    else {
      if resource.isDirectory then {
        val basePath = if requestPath.endsWith("/") then requestPath else requestPath + "/"
        resource = resourceManager.getResource(basePath + "/index.html")
      }
      if resource != null && resource.getName.endsWith(".html") then
        transformHtmlContent(exchange, resource.getFilePath)
      else
        // if not HTML, delegate to the next handler
        next.handleRequest(exchange)
    }
  }

  private def transformHtmlContent(exchange: HttpServerExchange, filePath: Path): Unit = {
    try {
      val originalContent = Files.readAllBytes(filePath)
      val transformedContent = transformHtml(new String(originalContent, StandardCharsets.UTF_8))
      exchange.getResponseHeaders.put(Headers.CONTENT_TYPE, "text/html")
      exchange.getResponseSender.send(ByteBuffer.wrap(transformedContent.getBytes))
    } catch {
      case e: IOException =>
        e.printStackTrace()
        exchange.setStatusCode(500)
        exchange.getResponseSender.send("Error transforming file.")
    }
  }

  private def transformHtml(htmlStr: String): String = {
    htmlStr.lastIndexOf("</body>") match {
      case -1 => htmlStr + InjectedScript
      case index =>
        val (before, after) = htmlStr.splitAt(index)
        before + InjectedScript + after
    }
  }

}
