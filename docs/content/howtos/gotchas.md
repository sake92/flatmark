---
title: Gotchas
description: Flatmark Gotchas
---

# {{page.title}}


## Can't add content to a template that extends another template
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


## Can't use TOC in a page
You must use it inside a template.
This is a technical limitation, because we take the header ids from markdown-generated HTML.



