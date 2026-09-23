---
title: Tutorials
description: Flatmark Tutorials
pagination:
  enabled: false
---

# {{page.title}}

Follow these lessons in order if you are new to Flatmark. Start with a working site, then add language variants,
structured data, and a local theme.

{%
set tutorials = [
    { "label": "Quickstart", "url": "/tutorials/quickstart.html" },
    { "label": "Multilingual Site", "url": "/tutorials/multilang.html" },
    { "label": "Data Files", "url": "/tutorials/data-files.html" },
    { "label": "Custom Local Theme", "url": "/tutorials/custom_local_theme.html" }
]
%}


{% for tut in tutorials %}
- [{{ tut.label }}]({{ tut.url }})
{% endfor %}




