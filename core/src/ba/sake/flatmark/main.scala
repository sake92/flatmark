package ba.sake.flatmark

import org.virtuslab.yaml.*

@main def cli() = {
  val siteRootFolder = os.pwd / "site1"
  val outputFolder = siteRootFolder / "_site"
  val layoutsFolder = siteRootFolder / "_layouts"
  val cacheFolder = siteRootFolder / ".flatmark-cache"
  val layoutTemplateHandler = new FlatmarkTemplateHandler(layoutsFolder.wrapped)
  val mdTemplateHandler = new FlatmarkTemplateHandler(siteRootFolder.wrapped)
  val markdownRenderer = new FlatmarkMarkdownRenderer(new NodejsInterop(cacheFolder))

  val layoutTemplatesMap = os
    .list(layoutsFolder)
    .filter(_.ext == "html")
    .map { file =>
      val layoutContent = os.read(file)
      file.baseName -> layoutContent
    }
    .toMap

  os.walk(siteRootFolder).filter(_.ext == "md").foreach { file =>
    // render markdown content
    val mdContentTemplateRaw = os.read(file)
    val (mdContentTemplate, frontMatter) = parseMd(mdContentTemplateRaw)
    val mdContent = mdTemplateHandler.render(mdContentTemplate, frontMatter)
    val mdHtmlContent = markdownRenderer.renderMarkdown(mdContent)

    // render final HTML file
    val layoutTemplate = layoutTemplatesMap("default")
    val htmlContent =
      layoutTemplateHandler.render(layoutTemplate, Map("title" -> "Flatmark Example", "content" -> mdHtmlContent))

    os.write.over(
      outputFolder / file.relativeTo(siteRootFolder).segments.init / s"${file.baseName}.html",
      htmlContent,
      createFolders = true
    )
  }

  def parseMd(mdTemplateRaw: String): (String, Map[String, String]) = {
    var firstTripleDashIndex = -1
    var secondTripleDashIndex = -1
    locally {
      val iter = mdTemplateRaw.linesIterator
      var i = 0
      while iter.hasNext && (firstTripleDashIndex == -1 || secondTripleDashIndex == -1) do {
        val line = iter.next()
        if (line.trim == "---") {
          if (firstTripleDashIndex == -1) firstTripleDashIndex = i
          else secondTripleDashIndex = i
        }
        i += 1
      }
    }
    if (firstTripleDashIndex != -1 && secondTripleDashIndex != -1) {
      val t = mdTemplateRaw.linesIterator
        .drop(secondTripleDashIndex + 1)
        .mkString("\n")
        .trim
      val fm = mdTemplateRaw.linesIterator
        .slice(firstTripleDashIndex + 1, firstTripleDashIndex + 1 + secondTripleDashIndex - firstTripleDashIndex - 1)
        .mkString("\n")
        .as[Map[String, String]]
        .toOption
        .getOrElse(Map())
      (t, fm)
    } else {
      (mdTemplateRaw, Map())
    }
  }
}
