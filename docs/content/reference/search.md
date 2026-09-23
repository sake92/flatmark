---
title: Search index
description: Flatmark generated search-index reference
---

# {{page.title}}

When `search.enabled` is `true`, Flatmark writes `_site/search/entries.json` after rendering all content and index pages.

The file contains a JSON array with one object per rendered page:

```json
[
  {
    "title": "Page title",
    "url": "/path/to/page.html",
    "text": "Rendered page text"
  }
]
```

| Field | Type | Description |
|---|---|---|
| `title` | string | Value of the page's `title` front-matter key, or `Untitled`. |
| `url` | string | Generated page URL, including `base_url` when configured. |
| `text` | string | Plain text extracted from the rendered content before the layout is applied. |

Flatmark generates the index but does not define a query protocol. Search interfaces load and filter the JSON in the
browser. The default theme supplies a `search-results.html` layout for this purpose.

See [Enable site search](/howtos/search.html) for setup instructions.
