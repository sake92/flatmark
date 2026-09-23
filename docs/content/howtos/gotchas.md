---
title: Troubleshoot templates and tables of contents
description: Resolve common Flatmark template limitations
---

# {{page.title}}

## Content disappears from a template that uses `extends`

When a Jinja template extends another template, put all child content inside blocks declared by the parent template.
Content outside those blocks is not rendered.

If `_layouts/base.html` declares this block:

```html
{% raw %}
{{ '{%' }} block content %}{{ '{%' }} endblock %}
{% endraw %}
```

Override that same block in the child template:

```html
{% raw %}
{{ '{%' }} extends "base.html" %}

{{ '{%' }} block content %}
  {{ '{{' }} page.content }}
{{ '{%' }} endblock %}
{% endraw %}
```

Do not add undeclared blocks to the child; the parent has no location in which to render them.

## A table of contents is empty in a content page

Render `page.toc` from a layout or include, not directly from a Markdown content file. Flatmark creates the table of
contents after it renders the Markdown headings, so the value is available during layout rendering.

Place the table-of-contents markup in an include such as `_includes/toc.html`, then include it from the page layout.
See the [`page.toc` reference](/reference/template-context.html#page-toc-items) for its fields.
