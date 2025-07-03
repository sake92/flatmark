---
title: Search
description: Flatmark Search
---

# {{page.title}}

After it processes all of the content, Flatmark will create a search index file 
named `_site/search/entries.json`.

It looks like this:

```json
[
  {
    "text": "Text of the page",
    "title": "Title of the page",
    "url": "/path/to/page.html"
  },
  ...
]
```

---

The next step is to create a search results page.
You need to create a file `content/search/results.md` with the following content:

```markdown
layout: search-results.html
```


The default theme will do the rest.
Note that `search.enabled` must be set to `true` in the `_config.yml` file (default is `true`).




