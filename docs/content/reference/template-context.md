---
title: Template Context
description: Flatmark Template Context Reference
---

# {{page.title}}

There are 3 contexts available in Flatmark templates:
- `site` - information about the whole site
- `page` - contains information about the current page
- `paginator` - contains information for pagination (only available on `index.md` pages)



## Site Context

Most of these are just propagated from the `_config.yaml` file.

They are available through the `site` context in templates.
For example, you can access the site name with {% raw %}`{{'{{'}} site.name }}`{% endraw %}.

```yaml
name: "My Site"
description: "My site description"
base_url: https://example.com

search:
  enabled: true
code_highlight:
    enabled: true
math_highlight:
    enabled: true

# languages used in the site
langs: [en, bs ...] 

# categories, folders in content/
categories: 
 blog:
   label: Blog
   description: ..
   # only available in index.md pages, empty otherwise
   # list of page contexts
   items:
     - title: Post 1,
       url: /blog/post-1.html
       ..

# YAML files from _data/ folder
data:
  my_data_file: "My data value"

```

The `langs`, `categories` and `data` are dynamic.



## Page Context

These are available through the `page` context in templates.
For example, you can access the page title with {% raw %}`{{'{{'}} page.title }}`{% endraw %}.

```yaml
title: ..
description: ..
content: ..                     # not empty only in _layouts/ and _includes/ 
lang: en
publish_date: ..
url: ..
layout: page.html

toc:
  - level: 1
    title: My Heading 1
    url: "#my-heading-1"
    children:
      - level: 2
        title: My Heading 2
        url: "#my-heading-2"
        ...
  
theme_props:
    my_theme_prop: my_value     # theme specific properties
```

## Paginator Context

This context is available only on `index.md` pages, and it provides information about pagination.

They are available through the `paginator` context in templates.
For example, you can access the site name with {% raw %}`{{'{{'}} paginator.per_page }}` {% endraw %}.


```yaml
enabled: true       # if pagination is enabled in page config front matter
items:              # list of page contexts
  - title: Post 1,
    url: /blog/post-1.html
    ...
per_page: 10
total_items: 25
total_pages: 3
current: 2          # 1-based index of the current page
prev: 1
next: 3
has_prev: true
has_next: true
prev_url: /blog/index-1.html
next_url: /blog/index-3.html
```