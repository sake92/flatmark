---
title: Tutorials
description: Flatmark Tutorials
pagination:
  enabled: false
---

# {{page.title}}

{%
set tutorials = [
    { "label": "Quickstart", "url": "/tutorials/quickstart.html" },
    { "label": "Examples", "url": "https://github.com/sake92/flatmark/tree/main/examples" }
]
%}


{% for tut in tutorials %}
- [{{ tut.label }}]({{ tut.url }})
{% endfor %}





