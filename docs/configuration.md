# Configuration

## Site Configuration

Edit `_config.yaml` in the root folder:
```yaml
name: My Cool Blog
description: My Cool Blog Description

categories:
  # content/blog/ folder
  blog:
    label: Blog
    description: The blog section of the site
  # content/docs/ folder
  docs:
    label: Documentation
    description: The documentation section of the site

tags:
  programming:
    label: Programming
    description: Posts related to programming

# default language
lang: en
timezone: Europe/Sarajevo
github_handle: sake92
twitter_handle: sake92
...
```


## Page Configuration

Put the configuration at the top of your page or post file, in YAML format, like this:
```yaml
---
title: Hello
tags: [scala, java]
publish_date: 2025-01-01 11:11
publish: yes | no | hide # hide means published but not listed/searched/google-indexed, default is yes
---

# Hello {{page.title}}

This is my first post!
```

The page configuration section is optional.
The file needs to start with `---` and the config ends with another `---`.  
This type of config is called "YAML front matter".
