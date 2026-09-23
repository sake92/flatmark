---
title: Data files
description: Render structured YAML data in a Flatmark page
---

# {{page.title}}

In this tutorial, you will add a YAML data file to the site from the [Quickstart](/tutorials/quickstart.html) and render
its values as a list.

## Add the data

Create an `_data` folder in the site root. Inside it, create `_data/authors.yaml`:

```yaml
- name: Ada
  skills: [mathematics, writing]
- name: Grace
  skills: [compilers, leadership]
```

Flatmark exposes a data file using its base name, so this file becomes `site.data.authors`.

## Render the authors

Create `content/authors.md`:

```markdown
---
title: Authors
---

# Authors

{% raw %}
{{ '{%' }} for author in site.data.authors %}
- **{{ '{{' }} author.name }}** — {{ '{{' }} author.skills|join(', ') }}
{{ '{%' }} endfor %}
{% endraw %}
```

Start the development server:

```shell
flatmark serve
```

Open <http://localhost:5555/authors.html>. You will see both authors and their comma-separated skills.

Change a value in `_data/authors.yaml` and save it. The rebuilt page will show the new value.

The completed structure matches the
[data-file example](https://github.com/sake92/flatmark/tree/main/examples/data-file).
