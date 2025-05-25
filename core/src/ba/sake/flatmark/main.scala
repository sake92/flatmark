package ba.sake.flatmark


@main def cli() =
  val wd = os.pwd / "site1"
  val templateHandler = new TemplateHandler()
  val res = templateHandler.render()
  val markdownHtml = MarkdownRenderer.renderMarkdown(res)
  os.write.over(
    wd / "_site" / "index.html",
    genHtml(markdownHtml),
    createFolders = true
  )

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
