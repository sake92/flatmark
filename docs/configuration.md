# Configuration

## Site Configuration

Edit `_config.yaml` in the root folder:
```yaml
title: My Cool Blog
description: My Cool Blog Description
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
date: 2025-01-01 11:11
layout: post
tags: scala, java
publish: yes | no | hide # hide means published but not listed/searched/google-indexed
---

# Hello {{page.title}}

This is my first post!
```

The page configuration needs to start with `---` and end with `---` to denote the YAML front matter.  
It needs to be the first thing in the file, before any other content.
