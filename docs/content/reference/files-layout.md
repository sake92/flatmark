---
title: Files Layout
description: Flatmark Files Layout
---

# {{page.title}}

## Content Structure

```bash
content/
├── index.md            <-- index page
├── blog                  <-- the blog category/
│   ├── index.md            <-- index page for blog posts
│   └── mypost.md           <-- one blog post
├── docs                <-- the docs category/
│   ├── index.md          <-- index page for docs
│   └── mypost.md         <-- one doc page
├── 404.md              <-- 404 not found page
└── bs/                 <-- translations live in lang-code/ named folders/
    ├── index.md
    └── blog/mypost.md    <-- translated post

static/                 <-- static files, copied as-is to the _site/
├── images/favicon.ico
├── styles/main.css
└── scripts/main.js
```

First level of folders in `content/` are categories.

The `index.md` (or `index.html`) pages are special.
They get the list of all pages in the category in the `paginator` argument, so you can list those items there.
For example, the `content/blog/index.md` would receive `paginator` with all the blog posts.


## Other

```bash
# config
_config.yaml        <-- global config

_layouts/           <-- templates
    default.peb       <-- default layout: for index page, about page etc
    post.peb          <-- layout for posts

_includes/          <-- snippets/fragments/helpers
    header.peb        <-- header snippet
    footer.peb        <-- footer snippet
    search.peb        <-- search form snippet
    pagination.peb    <-- pagination snippet

_i18n/              <-- translations for the site
    my_translations.yaml            <-- default language translations
    my_translations_de.yaml         <-- german translations

_themes/           <-- custom, local themes
    my_theme/          <-- local theme folder
        _layouts/         <-- local theme layouts
        _includes/        <-- local theme includes

_site/              <-- result of rendering, this will be deployed

.flatmark-cache     <-- cache for the flatmark results, can be deleted
```

Notice that all folders that have prefix `_` or `.` are some kind of configuration.

The `_config.yaml` file is the global configuration file.

The `_layouts/` folder contains templates for the pages.  
You can override a theme layout by creating a **file with the same name** in the `_layouts/` folder, like `_layouts/page.peb`.

The `_includes/` folder contains reusable snippets, like header, footer, search form, etc.
You can override a theme include by creating a **file with the same name** in the `_includes/` folder, like `_includes/footer.peb`.

The `_site/` folder is the output folder, where the rendered files will be placed.

The `.flatmark-cache` folder is used to store the cache for the flatmark results.  
These are:
- cached diagram rendering results
- downloaded themes

