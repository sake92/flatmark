---
title: Enable site search
description: Enable the generated search index and results page
---

# {{page.title}}

Enable search in `_config.yaml`:

```yaml
search:
  enabled: true
```

Build the site:

```shell
flatmark build
```

Flatmark writes the index to `_site/search/entries.json`. When the theme provides `search-results.html`, Flatmark also
generates `_site/search/results.html` automatically. The default theme provides this layout and a search form.

To customize the results page, create `content/search/results.md` with `layout: search-results.html` in its front matter.
That page takes precedence over the automatic page. A custom theme can provide the layout in its `_layouts/` folder, or
the site can provide one in its own `_layouts/` folder.
See the [search index reference](/reference/search.html) for the generated JSON format.
