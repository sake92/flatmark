

---
title: Reference
description: Flatmark Reference
theme_props:
  pagination_disabled: false
---

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



