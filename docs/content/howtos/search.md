---
title: Enable site search
description: Add the generated search index and default search results page
---

# {{page.title}}

Enable search in `_config.yaml`:

```yaml
search:
  enabled: true
```

Create `content/search/results.md`:

```markdown
---
title: Search results
layout: search-results.html
---
```

Build the site:

```shell
flatmark build
```

Flatmark writes the index to `_site/search/entries.json`. The default theme's `search-results.html` layout reads that
index and displays matching pages.

If a custom theme does not provide `search-results.html`, add an equivalent layout to the site's `_layouts/` folder.
See the [search index reference](/reference/search.html) for the generated JSON format.
