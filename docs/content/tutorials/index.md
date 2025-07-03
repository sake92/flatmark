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
    { "label": "Multilingual Site", "url": "/tutorials/multilang.html" }
]
%}


{% for tut in tutorials %}
- [{{ tut.label }}]({{ tut.url }})
{% endfor %}





