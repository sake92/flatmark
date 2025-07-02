---
title: Tutorials
description: Flatmark Tutorials
pagination:
  enabled: false
---



{%
set tutorials = [
    { "label": "Quickstart", "link": "/tutorials/quickstart.html" },
    { "label": "Examples", "link": "https://github.com/sake92/flatmark/tree/main/examples" }
]
%}


{% for tut in tutorials %}
- [{{ tut.label }}]({{ tut.link }})
{% endfor %}





