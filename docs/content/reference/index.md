

---
title: Reference
description: Flatmark Reference
pagination:
  enabled: false
---

# Reference

{%
set references = [
    { "label": "Configuration", "link": "/reference/configuration.html" },
    { "label": "Files layout", "link": "/reference/files-layout.html" },
    { "label": "Themes", "link": "/reference/themes.html" }
]
%}


{% for ref in references %}
- [{{ ref.label }}]({{ ref.link }})
{% endfor %}



