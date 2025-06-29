---
title: How Tos
description: How Tos
theme_props:
  pagination_disabled: true
---


## How to make a series of tutorials?

{# we need {% raw %} for first pass (content) 
and {{ '{%' }} for second (layout) #}
```markdown
Tutorials:
{% raw %}
{{ '{%' }} set tutorials = [
    { "label": "My tutorial 1", "link": "/tutorials/tutorial1.html" },
    { "label": "My tutorial 2", "link": "/tutorials/tutorial2.html" }
] %}
{% endraw  %}
```




----


## Gotchas

### Can't add content to a template that extends another template
When you extend a template:
```markdown
{% raw %}
{{ '{%' }} extends "base.html" %}

{{ '{%' }} block title %}
    {{ '{{' }}page.title}}
{{ '{%' }} endblock %}

---
cant add this!!!

{{ '{%' }} block hack %}
    cant add this either!!!
{{ '{%' }} endblock %}
{% endraw  %}
```

note that **you can only override blocks that are defined in the base template**,  
you cannot create new blocks or add content.


### Can't use TOC in a page
You must use it inside a template.
This is a technical limitation, because we take the header ids from markdown-generated HTML.


