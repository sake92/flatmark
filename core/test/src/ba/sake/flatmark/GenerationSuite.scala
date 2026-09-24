package ba.sake.flatmark

import ba.sake.flatmark.generation.FlatmarkGenerator
import ba.sake.flatmark.ssr.WebDriverHolder

import java.util.Locale

class GenerationSuite extends munit.FunSuite {

  test("a failed build leaves the previously generated site intact") {
    val siteRoot = site()
    os.write(siteRoot / "content/index.md", "# Home", createFolders = true)
    os.write(siteRoot / "_site/keep.txt", "previous site", createFolders = true)

    assert(!generate(siteRoot))
    assertEquals(os.read(siteRoot / "_site/keep.txt"), "previous site")
  }

  test("a successful build replaces the previous site and removes stale files") {
    val siteRoot = site()
    writeLayouts(siteRoot)
    os.write(siteRoot / "content/index.md", "# Home", createFolders = true)
    os.write(siteRoot / "_site/stale.txt", "stale", createFolders = true)

    assert(generate(siteRoot))
    assert(os.exists(siteRoot / "_site/index.html"))
    assert(!os.exists(siteRoot / "_site/stale.txt"))
  }

  test("an interrupted publication restores its backup before the next build") {
    val siteRoot = site()
    os.write(siteRoot / "content/index.md", "# Home", createFolders = true)
    os.write(
      siteRoot / ".flatmark-cache/builds/previous-site/keep.txt",
      "previous site",
      createFolders = true
    )

    assert(!generate(siteRoot))
    assertEquals(os.read(siteRoot / "_site/keep.txt"), "previous site")
  }

  test("disabled pagination renders all category items on one index page") {
    val siteRoot = site(
      """theme:
        |  enabled: false
        |search:
        |  enabled: false
        |categories:
        |  blog:
        |    label: Blog
        |""".stripMargin
    )
    writeLayouts(
      siteRoot,
      index = """<!doctype html><html><body>
          |{% for item in paginator.items %}<span class="item">{{ item.title }}</span>{% endfor %}
          |</body></html>
          |""".stripMargin
    )
    os.write(
      siteRoot / "content/blog/index.md",
      """---
        |pagination:
        |  enabled: false
        |  per_page: 1
        |---
        |# Blog
        |""".stripMargin,
      createFolders = true
    )
    os.write(siteRoot / "content/blog/first.md", "---\ntitle: First\n---\n# First")
    os.write(siteRoot / "content/blog/second.md", "---\ntitle: Second\n---\n# Second")

    assert(generate(siteRoot))
    val index = os.read(siteRoot / "_site/blog/index.html")
    assert(index.contains(">First</span>"))
    assert(index.contains(">Second</span>"))
    assert(!os.exists(siteRoot / "_site/blog/index-2.html"))
  }

  test("rendering another language does not change the JVM display locale") {
    val originalLocale = Locale.getDefault(Locale.Category.DISPLAY)
    val siteRoot = site("lang: bs\ntheme:\n  enabled: false\nsearch:\n  enabled: false\n")
    writeLayouts(siteRoot)
    os.write(siteRoot / "content/index.md", "# Početna", createFolders = true)

    assert(generate(siteRoot))
    assertEquals(Locale.getDefault(Locale.Category.DISPLAY), originalLocale)
  }

  test("a blank data file is loaded as an empty map") {
    val siteRoot = site()
    writeLayouts(siteRoot)
    os.write(siteRoot / "_data/empty.yaml", "", createFolders = true)
    os.write(siteRoot / "content/index.md", "# Home", createFolders = true)

    assert(generate(siteRoot))
  }

  test("search creates a results page from its layout and respects a site page override") {
    val siteRoot = site("theme:\n  enabled: false\nsearch:\n  enabled: true\n")
    writeLayouts(siteRoot)
    os.write(siteRoot / "_layouts/search-results.html", "<html><body>Default {{ page.title }}</body></html>")
    os.write(siteRoot / "content/index.md", "# Home", createFolders = true)

    assert(generate(siteRoot))
    assert(os.read(siteRoot / "_site/search/results.html").contains("Default Search"))
    assert(os.exists(siteRoot / "_site/search/entries.json"))

    os.write(
      siteRoot / "content/search/results.md",
      "---\ntitle: Custom search\nlayout: search-results.html\n---\n# Search",
      createFolders = true
    )
    assert(generate(siteRoot))
    assert(os.read(siteRoot / "_site/search/results.html").contains("Default Custom search"))
  }

  private def site(config: String = "theme:\n  enabled: false\nsearch:\n  enabled: false\n"): os.Path = {
    val root = os.temp.dir(prefix = "flatmark-generation-")
    os.write(root / "_config.yaml", config)
    root
  }

  private def writeLayouts(
      siteRoot: os.Path,
      page: String = "<!doctype html><html><body>{{ page.content }}</body></html>",
      index: String = "<!doctype html><html><body>{{ page.content }}</body></html>"
  ): Unit = {
    os.write(siteRoot / "_layouts/page.html", page, createFolders = true)
    os.write(siteRoot / "_layouts/index.html", index)
  }

  private def generate(siteRoot: os.Path): Boolean = {
    val webDriverHolder = WebDriverHolder()
    try FlatmarkGenerator("", webDriverHolder, updateTheme = false).generate(siteRoot, useCache = false)
    finally webDriverHolder.quit()
  }
}
