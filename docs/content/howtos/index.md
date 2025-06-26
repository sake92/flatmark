---
title: How Tos
description: How Tos
theme_props:
  pagination_disabled: true
---


## How to make a series of tutorials?

```markdown
Tutorials:
{% verbatim %}
{%
set tutorials = [
    { "label": "My tutorial 1", "link": "/tutorials/tutorial1.html" },
    { "label": "My tutorial 2", "link": "/tutorials/tutorial2.html" }
]
%}


{% for tutorial in tutorials %}
- [{{ tutorial.label }}]({{ tutorial.link }})
{% endfor %}
{% endverbatim  %}
```




----


## Gotchas

### Can't add content to a template that extends another template
When you extend a template:
```markdown
{% verbatim %}
{% extends "base" %}

{% block title %}
    {{page.title}}
{% endblock %}

---
<div>cant add this!!!</div>

{% block hack %}
    <div>cant add this either!!!</div>
{% endblock %}
{% endverbatim  %}
```

note that **you can only override blocks that are defined in the base template**,  
you cannot create new blocks or add content.


### Can't use TOC in a page
You must use it inside a template.
This is a technical limitation, because we take the header ids from markdown-generated HTML.


