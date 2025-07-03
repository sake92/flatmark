---
title: Configuration
description: Flatmark Configuration Reference
---

# Configuration

## Site Configuration

For global site config you edit the `_config.yaml` in the root folder:
```yaml
name: My Cool Blog                      # website name
description: My Cool Blog Description   # website description
base_url: https://mydomain.com/subpath  # URL where website will be deployed

theme:
  enabled: true                         # enable theme
  source: my_local_theme                # website theme

search:
  enabled: true                         # enable search
code_highlight:
  enabled: true                         # enable code highlighting
math_highlight:
  enabled: true                         # enable math highlighting

categories:
  blog:                                 # content/blog/ folder
    label: Blog
  docs:                                 # content/docs/ folder
    label: Documentation


lang: en                                # default language
timezone: Europe/Sarajevo
...
```

The `base_url` can also be specified as an environment variable `FLATMARK_BASE_URL`.

## Page Configuration

Put the configuration at the top of your page file, in YAML format, like this:
```yaml
---
title: Hello
description: My description
publish_date: 2025-01-01 11:11    # in timezone specified in the _config.yaml
layout: page.html
ext: html                         # extension of the generated file, default is html

publish: yes
tags: [scala, java]

pagination:
  enabled: true                   # enable pagination for this index.md page
  per_page: 10                    # number of items per page
  sort_by: -publish_date          # sort by publish date, descending

theme_props:
  my_theme_prop: my_value         # theme specific properties
---

{% raw %}
# Hello {{ '{{' }}page.title{{ '}}' }}
{% endraw %}

This is my first post!
```

The page configuration section is optional.  
If used, the file needs to start with `---`, the config ends with another `---`.  
This type of config is called "YAML front matter".
