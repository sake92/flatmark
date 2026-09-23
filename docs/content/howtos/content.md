---
title: Create an ordered tutorial series
description: Render a manually ordered list of pages with Jinja
---

# {{page.title}}

Use an explicit Jinja list when a series must follow a fixed order instead of Flatmark's pagination sort order.

In the series index page, define each entry and render the list:

{# The outer raw block protects Jinja intended for the documented site. #}
```markdown
{% raw %}
{{ '{%' }} set tutorials = [
    { "label": "Install the application", "url": "/tutorials/install.html" },
    { "label": "Create a project", "url": "/tutorials/create-project.html" },
    { "label": "Deploy the project", "url": "/tutorials/deploy.html" }
] %}

{{ '{%' }} for tutorial in tutorials %}
- [{{ '{{' }} tutorial.label }}]({{ '{{' }} tutorial.url }})
{{ '{%' }} endfor %}
{% endraw %}
```

Set `pagination.enabled` to `false` in the page's front matter so the index is emitted as one page:

```yaml
---
title: Tutorials
pagination:
  enabled: false
---
```

The rendered links retain the order of the Jinja list.
