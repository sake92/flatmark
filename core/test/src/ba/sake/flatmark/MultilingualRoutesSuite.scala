package ba.sake.flatmark

import ba.sake.flatmark.ssr.WebDriverHolder
import ba.sake.flatmark.generation.FlatmarkGenerator

class MultilingualRoutesSuite extends munit.FunSuite {

  test("only existing categories and same-path translations are linked") {
    val siteRoot = os.temp.dir(prefix = "flatmark-multilang-")
    os.write(
      siteRoot / "_config.yaml",
      """lang: bs
        |theme:
        |  enabled: false
        |search:
        |  enabled: false
        |categories:
        |  java:
        |    label: Java
        |  scala:
        |    label: Scala
        |""".stripMargin
    )
    val layout =
      """<!doctype html>
        |<html><body>
        |{% for catKey, catValue in site.categories.items() %}
        |<span class="category">{{ catKey }}</span>
        |{% endfor %}
        |{% for lang in site.langs %}
        |<a class="language" href="{{ lang.url }}">{{ lang.code }}</a>
        |{% endfor %}
        |{{ page.content }}
        |</body></html>
        |""".stripMargin
    os.write(siteRoot / "_layouts/page.html", layout, createFolders = true)
    os.write(siteRoot / "_layouts/index.html", layout)

    os.write(siteRoot / "content/index.md", "# Početna", createFolders = true)
    os.write(siteRoot / "content/java/index.md", "# Java", createFolders = true)
    os.write(siteRoot / "content/scala/index.md", "# Scala", createFolders = true)
    os.write(siteRoot / "content/article.md", "# Članak")
    os.write(siteRoot / "content/en/index.md", "# Home", createFolders = true)
    os.write(siteRoot / "content/en/scala/index.md", "# Scala", createFolders = true)
    os.write(siteRoot / "content/en/article.md", "# Article")

    val webDriverHolder = WebDriverHolder()
    val generated =
      try FlatmarkGenerator("", webDriverHolder, updateTheme = false).generate(siteRoot, useCache = false)
      finally webDriverHolder.quit()

    assert(generated)

    val englishHome = os.read(siteRoot / "_site/en/index.html")
    assert(!englishHome.contains("<span class=\"category\">java</span>"))
    assert(englishHome.contains("<span class=\"category\">scala</span>"))

    val defaultArticle = os.read(siteRoot / "_site/article.html")
    assert(defaultArticle.contains("href=\"/en/article.html\">en</a>"))

    val defaultJava = os.read(siteRoot / "_site/java/index.html")
    assert(defaultJava.contains("href=\"/en\">en</a>"))
  }
}
