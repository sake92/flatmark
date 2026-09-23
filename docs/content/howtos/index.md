---
title: How-to guides
description: Goal-oriented guides for working with Flatmark
pagination:
  enabled: false
---


# {{page.title}}

Use these guides when you already have a Flatmark site and need to complete a specific task.



{%
set howtos = [
    { "label": "Create an ordered tutorial series", "url": "/howtos/content.html" },
    { "label": "Enable site search", "url": "/howtos/search.html" },
    { "label": "Troubleshoot templates and tables of contents", "url": "/howtos/gotchas.html" }
]
%}


{% for howto in howtos %}
- [{{ howto.label }}]({{ howto.url }})
{% endfor %}
