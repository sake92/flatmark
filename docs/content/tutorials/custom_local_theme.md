---
title: Custom local theme
description: Build a minimal Flatmark theme with shared layouts
---

# {{page.title}}

In this tutorial, you will replace the default theme in the [Quickstart](/tutorials/quickstart.html) site with a small
local theme.

## Create the theme structure

Create these folders in the site root:

```shell
mkdir -p _themes/my-theme/_layouts
```

The site now contains:

```text
my-site/
├── content/
│   └── index.md
└── _themes/
    └── my-theme/
        └── _layouts/
```

## Add the shared layout

Create `_themes/my-theme/_layouts/base.html`:

```html
<!doctype html>
<html lang="{% raw %}{{ '{{' }} page.lang.code }}{% endraw %}">
  <head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>{% raw %}{{ '{{' }} page.title }} · {{ '{{' }} site.name }}{% endraw %}</title>
  </head>
  <body>
    <main>
{% raw %}
      {{ '{%' }} block content %}{{ '{%' }} endblock %}
{% endraw %}
    </main>
  </body>
</html>
```

The `content` block provides a location for child layouts to insert a rendered page.

## Add page layouts

Create `_themes/my-theme/_layouts/index.html`:

```html
{% raw %}
{{ '{%' }} extends "base.html" %}
{{ '{%' }} block content %}
  {{ '{{' }} page.content }}
{{ '{%' }} endblock %}
{% endraw %}
```

Create `_themes/my-theme/_layouts/page.html` with the same content. Flatmark selects `index.html` for files named
`index.md` and `page.html` for other content files.

## Select the theme

Create or update `_config.yaml`:

```yaml
name: My themed site
theme:
  source: my-theme
```

Run the site:

```shell
flatmark serve
```

Open <http://localhost:5555>. The home page is now rendered through the local theme. Add `content/about.md` and open
`/about.html` to confirm that the `page.html` layout works too.

See the [themes reference](/reference/themes.html) for supported theme folders, remote sources, and site-level overrides.
