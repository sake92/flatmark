

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
    { "label": "Themes", "url": "/reference/themes.html" }
]
%}


{% for ref in references %}
- [{{ ref.label }}]({{ ref.url }})
{% endfor %}



