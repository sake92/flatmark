---
title: Configuration
description: Flatmark site and page configuration reference
---

# {{page.title}}

Flatmark configuration keys use `snake_case`.

## Site configuration

Site configuration is read from `_config.yaml` in the site root. Every key is optional.

| Key | Type | Default | Description |
|---|---|---|---|
| `name` | string | `My Site` | Site name exposed as `site.name`. |
| `description` | string | empty | Site description exposed as `site.description`. |
| `base_url` | string or null | null | URL prefix added to root-relative links and generated page URLs. |
| `lang` | language tag | `en` | Default content language. |
| `timezone` | time-zone ID | system default | Zone applied to page `publish_date` values. |
| `theme` | object | see below | Theme source and enablement. |
| `search` | object | see below | Search-index generation. |
| `categories` | mapping | empty | Category keys and their labels. |
| `tags` | mapping | empty | Tag keys exposed to templates. |
| `code_highlight` | object | see below | Code highlighting during the build. |
| `math_highlight` | object | see below | Math rendering during the build. |

Example:

```yaml
name: My site
description: Notes and articles
base_url: https://example.com/docs
lang: en
timezone: Europe/Sarajevo

theme:
  enabled: true
  source: https://github.com/sake92/flatmark-themes?branch=main&folder=default

search:
  enabled: true
code_highlight:
  enabled: true
math_highlight:
  enabled: true

categories:
  blog:
    label: Blog
    description: Recent articles

tags:
  scala:
    label: Scala
    description: Articles about Scala
```

### Theme

| Key | Type | Default | Description |
|---|---|---|---|
| `theme.enabled` | boolean | `true` | Enables theme loading. |
| `theme.source` | string | Flatmark's default remote theme | Local theme name or HTTP(S) Git repository URL. |

See the [themes reference](/reference/themes.html) for source URL parameters and override behavior.

### Feature switches

| Key | Type | Default | Description |
|---|---|---|---|
| `search.enabled` | boolean | `true` | Generates `_site/search/entries.json`. |
| `code_highlight.enabled` | boolean | `true` | Exposed as `site.code_highlight.enabled` for themes. |
| `math_highlight.enabled` | boolean | `true` | Exposed as `site.math_highlight.enabled` for themes. |

### Categories and tags

Each entry in `categories` and `tags` has a required `label` and an optional `description`, which defaults to an empty
string. A category appears in a language's template context only when that language contains the category's `index.md`
or `index.html`.

### Base URL environment fallback

`FLATMARK_BASE_URL` supplies `base_url` when the key is absent from `_config.yaml`. A configured `base_url` takes
precedence. Trailing `/` characters are removed from the resolved value.

## Page configuration

Page configuration is YAML front matter at the beginning of a Markdown or HTML content file. The opening and closing
`---` delimiters are required when front matter is present.

| Key | Type | Default | Description |
|---|---|---|---|
| `layout` | string or null | `index.html` for index pages; `page.html` otherwise | Layout template name. |
| `title` | string | `Untitled` | Page title. |
| `description` | string | empty | Page description. |
| `publish_date` | local date-time or null | null | Date and time interpreted in the site time zone. |
| `ext` | string or null | `html` | Generated filename extension. Must be one path segment. |
| `pagination` | object | see below | Pagination behavior for index pages. |
| `theme_props` | string mapping | empty | Theme-specific values exposed as `page.theme_props`. |

Example:

```yaml
---
title: Recent posts
description: The latest articles
publish_date: 2025-01-01 11:11
layout: index.html
ext: html
pagination:
  enabled: true
  per_page: 10
  sort_by: -publish_date
theme_props:
  show_author: "true"
---
```

### Pagination

| Key | Type | Default | Description |
|---|---|---|---|
| `pagination.enabled` | boolean | `true` | Splits a non-empty index into multiple pages. |
| `pagination.per_page` | positive integer | `10` | Maximum items on each page. |
| `pagination.sort_by` | string | `-publish_date` | Sort order: `publish_date`, `-publish_date`, `title`, or `-title`. |
