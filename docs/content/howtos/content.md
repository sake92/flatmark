---
title: Content How Tos
description: How to do things with Flatmark content
---

# {{page.title}}


## How to make a series of tutorials?

The default pagination might not fit your use case.
For example, you might want to have a series of tutorials, 
which are ordered in a fixed way (not by publish date or title..).

For this, you can use Jinja variables to define a list of tutorials.
Then use a for loop to render them:

{# we need {% raw %} for first pass (content) 
and {{ '{%' }} for second (layout) #}
```markdown
Tutorials:
{% raw %}
{{ '{%' }} set tutorials = [
    { "label": "My tutorial 1", "url": "/tutorials/tutorial1.html" },
    { "label": "My tutorial 2", "url": "/tutorials/tutorial2.html" }
] %}
{% endraw  %}

{% raw %}
{{ '{%' }} for tut in tutorials %}
- [{{ '{{' }} tut.label }}]({{ '{{' }} tut.url }})
{{ '{%' }} endfor %}
{% endraw  %}
```


