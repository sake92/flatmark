

---
title: Reference
description: Flatmark Reference
pagination:
  enabled: false
---

# {{page.title}}

{%
set references = [
    { "label": "Configuration", "url": "/reference/configuration.html" },
    { "label": "Files layout", "url": "/reference/files-layout.html" },
    { "label": "Template Context", "url": "/reference/template-context.html" },
    { "label": "Themes", "url": "/reference/themes.html" },
    { "label": "Search", "url": "/reference/search.html" }
]
%}


{% for ref in references %}
- [{{ ref.label }}]({{ ref.url }})
{% endfor %}



