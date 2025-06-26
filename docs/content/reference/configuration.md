---
title: Configuration
---

# Configuration

## Site Configuration

For global site config you edit the `_config.yaml` in the root folder:
```yaml
name: My Cool Blog
description: My Cool Blog Description
theme: my_theme
base_url: https://sake92.github.io/flatmark

categories:
  # content/blog/ folder
  blog:
    label: Blog
  # content/docs/ folder
  docs:
    label: Documentation

# default language
lang: en
timezone: Europe/Sarajevo
...
```

The `base_url` can also be specified as an environment variable `FLATMARK_BASE_URL`.

## Page Configuration

Put the configuration at the top of your page file, in YAML format, like this:
```yaml
---
title: Hello
tags: [scala, java]
publish_date: 2025-01-01 11:11  # in timezone specified in the _config.yaml
publish: yes | no | hide        # hide means published but not listed/searched/google-indexed, default is yes
---

# Hello {{page.title}}

This is my first post!
```

The page configuration section is optional.  
The file needs to start with `---` and the config ends with another `---`.  
This type of config is called "YAML front matter".
