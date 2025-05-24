package ba.sake.flatmark

val mdSource = """
# Hello World
This is a **markdown** example with *italic* and `code`.

---

```scala
val x = 1
```

---

```math
x = 5
```

---

```diagram:graphviz
digraph G {Hello->World}
```

"""

@main def cli() =
  val wd = os.pwd / "site1"
  val markdownHtml = MarkdownRenderer.renderMarkdown(mdSource)
  os.write.over(
    wd / "dest" / "index.html",
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
        <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/highlight.js/11.11.1/styles/default.min.css">
    </head>
    <body>
        ${bodyContent}
    </body>
    </html>
    """
}
