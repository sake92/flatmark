package ba.sake.flatmark

@main def cli() =
  val siteRootFolder = os.pwd / "site1"
  val outputFolder = siteRootFolder / "_site"
  val layoutsFolder = siteRootFolder / "_layouts"
  val cacheFolder = siteRootFolder / ".flatmark-cache"
  val layoutTemplateHandler = new TemplateHandler(layoutsFolder.wrapped)
  val mdTemplateHandler = new TemplateHandler(siteRootFolder.wrapped)
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
    // render markdown file
    // TODO read YAML front matter and pass it to the template
    val mdContentTemplate = os.read(file)
    val mdContent = mdTemplateHandler.render(mdContentTemplate, Map("title" -> "Flatmark Example"))
    val mdHtmlContent = markdownRenderer.renderMarkdown(mdContent)

    // render final HTML file
    val layoutTemplate = layoutTemplatesMap("default")
    val htmlContent =
      layoutTemplateHandler.render(layoutTemplate, Map("title" -> "Flatmark Example", "content" -> mdHtmlContent))

    os.write.over(
      outputFolder / file.relativeTo(siteRootFolder).segments.init / s"${file.baseName}.html",
      genHtml(htmlContent),
      createFolders = true
    )
  }

def genHtml(bodyContent: String): String = {
  s"""
    <!DOCTYPE html>
    <html lang="en">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>Flatmark</title>
        <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/highlight.js/11.11.1/styles/a11y-dark.min.css">
        <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/katex@0.16.22/dist/katex.min.css">
        <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/water.css@2/out/water.css">
    </head>
    <body>
        ${bodyContent}
    </body>
    </html>
    """
}
