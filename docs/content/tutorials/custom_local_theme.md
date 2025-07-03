---
title: Custom Local Theme
description: Flatmark Custom Theme tutorial
---

# {{page.title}}

This tutorial builds on the [Quickstart](/tutorials/quickstart.html) tutorial.
It shows how to use a local theme with Flatmark.

## Configuring a Local Theme
Add this to `_config.yaml`:
```yaml
theme:
  source: my_theme
```

This tells Flatmark to use our local theme called `my_theme`.
It needs to be in the `_themes/` folder of your site.

The build fails because it can't find the theme:
```shell
ba.sake.flatmark.FlatmarkException: Local theme folder does not exist. Please create it or use a valid theme URL.
```

Create a new folder in `my_site/`, called `_themes/my_theme/`.
This part is fixed now.

## Creating a Layout for index.md

But we have a different error:
```shell
ba.sake.flatmark.FlatmarkException: Template 'index.html' not found in content/ or _layouts/ or theme _layouts/.
```

We are missing the `index.html` layout, so let's fix that.  
Inside `_themes/my_theme/` create a file `_layouts/index.html`.

> The `index.html` layout is used for all `index.md` files. (can be overriden)

Now the build succeeds.
The files structure should look like this:
```
my_site/
├── _config.yaml
├── content/
│   └── index.md
└── _themes/
    └── my_theme/
        └── _layouts/
            └── index.html
```


---
But we get an empty page because we don't have any content in the layout.
Let's add some content to the `index.html` layout:
```html
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta name="color-scheme" content="light dark">
    <title>{% raw %}{{ '{{' }} site.name {{ '}}' }}{% endraw %}</title>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/highlight.js/11.11.1/styles/a11y-dark.min.css">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/katex@0.16.22/dist/katex.min.css">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/water.css@2/out/water.css">
</head>
<body>
<main>
    {% raw %}{{'{{'}} page.content {{'}}'}} {% endraw %}
</main>

</body>
</html>
```

Success, now it works!

The curly braces `{{ }}` are used to insert variables in the template.  
This is a Jinja template, so we can use Jinja syntax to insert variables, 
use control structures and its filters etc.

## Adding More Pages

Let's add another page: `about.md` in the `content/` folder:
```markdown
# About Me
```

We get a similar error:
```shell
ba.sake.flatmark.FlatmarkException: Template 'page.html' not found in content/ or _layouts/ or theme _layouts/.
```

Now we are missing the `page.html` layout.

At this point, we can copy-paste the `index.html` layout to `page.html` and it will work.
But this is not a good practice.
Let's rename the `index.html` layout to `base.html`, and replace the `<main>` tag with this:
```html
{% raw %}
{{ '{%' }} block content %}
{{ '{%' }} endblock %} 
{% endraw %}
```

The `content` block is like a slot where each page can insert its content.

---
We need to update the `index.html` layout to extend the `base.html` layout:
```html
{% raw %}
{{ '{%' }} extends "base.html" %}

{{ '{%' }} block content %}
    {{'{{'}} page.content {{'}}'}}
{{ '{%' }} endblock %} 

{% endraw %}
```

---
And the `page.html`:
```html
{% raw %}
{{ '{%' }} extends "base.html" %}

{{ '{%' }} block content %}
    {{'{{'}} page.content {{'}}'}}

    Custom content for the page goes here.
{{ '{%' }} endblock %}
{% endraw %}
```




