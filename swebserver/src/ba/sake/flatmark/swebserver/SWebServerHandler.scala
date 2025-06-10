package ba.sake.flatmark.swebserver

import java.nio.file.Path
import java.nio.ByteBuffer
import java.io.IOException
import java.nio.file.Files
import io.undertow.server.HttpHandler
import io.undertow.server.HttpServerExchange
import io.undertow.server.handlers.resource.PathResourceManager
import io.undertow.util.Headers
import scala.util.boundary

class SWebServerHandler(baseFolder: Path, next: HttpHandler) extends HttpHandler {
  
  private val resourceManager = new PathResourceManager(baseFolder, 100 * 1024 * 1024)

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

  // TODO inject websocket listener and refresh page
  private def transformHtmlContent(exchange: HttpServerExchange, filePath: Path): Unit = {
    try {
      val originalContent = Files.readAllBytes(filePath)
      val transformedContent = new String(originalContent, "utf8") + "\n// Transformed on the fly!"
      exchange.getResponseHeaders.put(Headers.CONTENT_TYPE, "text/html")
      exchange.getResponseSender.send(ByteBuffer.wrap(transformedContent.getBytes))
    } catch {
      case e: IOException =>
        e.printStackTrace()
        exchange.setStatusCode(500)
        exchange.getResponseSender.send("Error transforming file.")
    }
  }
}
