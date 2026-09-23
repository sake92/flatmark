

---
title: Reference
description: Flatmark Reference
pagination:
  enabled: false
---

# {{page.title}}

Technical reference for Flatmark's command-line interface, configuration, content model, and templates.

{%
set references = [
    { "label": "CLI", "url": "/reference/cli.html" },
    { "label": "Configuration", "url": "/reference/configuration.html" },
    { "label": "Files layout", "url": "/reference/files-layout.html" },
    { "label": "Markdown and rendering", "url": "/reference/markdown.html" },
    { "label": "Template Context", "url": "/reference/template-context.html" },
    { "label": "Themes", "url": "/reference/themes.html" },
    { "label": "Search", "url": "/reference/search.html" }
]
%}


{% for ref in references %}
- [{{ ref.label }}]({{ ref.url }})
{% endfor %}


