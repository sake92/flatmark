---
title: File layout
description: Flatmark site folders and routing conventions
---

# {{page.title}}

## Site root

| Path | Purpose |
|---|---|
| `_config.yaml` | Global site configuration. |
| `content/` | Markdown and HTML source pages. |
| `static/` | Files copied unchanged to `_site/`. |
| `_data/` | Top-level YAML data files exposed through `site.data`. |
| `_layouts/` | Site layout overrides. |
| `_includes/` | Site include overrides. |
| `_i18n/` | Resource bundles used by Jinja. |
| `_sass/` | Sass sources compiled to `_site/styles/`; requires a `sass` executable. |
| `_themes/` | Local themes. |
| `_site/` | Generated site output. |
| `.flatmark-cache/` | Downloaded themes, rendered-fragment cache, and transactional build data. |

`_site/` and `.flatmark-cache/` are generated and disposable. A failed build preserves the previous successful `_site/`.

## Content and routes

The path below `content/` determines the generated file path:

```text
content/index.md          -> _site/index.html          -> /
content/about.md          -> _site/about.html          -> /about.html
content/blog/index.md     -> _site/blog/index.html     -> /blog
content/blog/first.md     -> _site/blog/first.html     -> /blog/first.html
```

The `ext` page setting replaces the default `html` extension. Directory-style URLs apply only to `index` pages whose
extension is `html`.

Files or folders below `content/` whose name starts with `_` or `.` are skipped. Supported source extensions are `.md`
and extensions ending in `.html`.

## Index pages and categories

Files named `index.md` or `index.html` use `index.html` as their default layout and receive a `paginator` context. Other
content files use `page.html` by default.

A first-level folder acts as a category when the same key is declared in `_config.yaml`. Its index receives the
category's non-index content items. See [Template context](/reference/template-context.html) for the available values.

## Translations

A first-level folder named with an ISO 639-1 two-letter language code is treated as a translation root:

```text
content/
├── index.md
├── about.md
└── bs/
    ├── index.md
    └── about.md
```

The relative path identifies matching translations. A missing translation links to that language's home page.

## Theme overlay

Flatmark loads theme files from `static/`, `_layouts/`, `_includes/`, and `_i18n/`. A site file at the corresponding
path takes precedence for layouts and includes; site static files are copied after theme static files and replace files
at matching paths.
