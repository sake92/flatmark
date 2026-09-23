---
title: Template context
description: Variables available to Flatmark Jinja templates
---

# {{page.title}}

Flatmark supplies three top-level objects to Jinja templates: `site`, `page`, and `paginator`.

## `site`

| Field | Type | Description |
|---|---|---|
| `name` | string | Site name from `_config.yaml`. |
| `description` | string | Site description from `_config.yaml`. |
| `base_url` | string or null | Configured base URL. |
| `langs` | list | Languages available for the current logical page. |
| `search.enabled` | boolean | Search-index feature switch. |
| `categories` | mapping | Categories available in the current language. |
| `tags` | mapping | Configured tags. |
| `code_highlight.enabled` | boolean | Code-highlighting feature switch. |
| `math_highlight.enabled` | boolean | Math-rendering feature switch. |
| `data` | mapping | YAML documents loaded from `_data/`. |

### `site.langs` items

| Field | Type | Description |
|---|---|---|
| `code` | string | BCP 47 language tag. |
| `label` | string | Language name displayed in that language. |
| `url` | string | Matching translation URL, or the language home URL when no translation exists. |

### `site.categories` entries

Each configured category is keyed by its folder name.

| Field | Type | Description |
|---|---|---|
| `label` | string | Configured label. |
| `description` | string | Configured description. |
| `items` | list of page objects | Non-index pages in the category and current language. Empty while ordinary content pages are rendered. |

A category is present only when its `index.md` or `index.html` exists for the current language.

### `site.tags` entries

Each configured tag has `label` and `description` string fields.

### `site.data`

Every top-level `.yaml` file in `_data/` becomes a field named after the file. For example, `_data/authors.yaml` is
available as `site.data.authors`. Nested YAML mappings and sequences retain their structure.

## `page`

| Field | Type | Description |
|---|---|---|
| `layout` | string | Selected layout template. |
| `title` | string | Page title. |
| `description` | string | Page description. |
| `content` | HTML string | Rendered page body. Populated when the layout is rendered. |
| `lang` | language object | Current language, with `code`, `label`, and `url`. |
| `publish_date` | zoned date-time or null | Publication date in the configured site time zone. |
| `rootRelPath` | string | Generated file path relative to `_site/`. |
| `url` | string | Generated URL, including `base_url` when configured. |
| `theme_props` | mapping | Theme-specific front-matter values. |
| `toc` | list | Heading hierarchy. Populated when the layout is rendered. |

### `page.toc` items

| Field | Type | Description |
|---|---|---|
| `level` | integer | HTML heading level from 1 through 6. |
| `title` | string | Heading text. |
| `url` | string | Fragment URL beginning with `#`. |
| `children` | list | Nested heading items. |

## `paginator`

`paginator` is populated for `index.md` and `index.html` pages. It is an empty mapping on other pages.

| Field | Type | Description |
|---|---|---|
| `enabled` | boolean | Whether pagination is enabled in front matter. |
| `items` | list of page objects | Items assigned to the current page. |
| `per_page` | integer | Configured page size. |
| `total_items` | integer | Number of items across all pages. |
| `total_pages` | integer | Number of generated pages. |
| `current` | integer | Current one-based page number. |
| `prev` | integer | Previous page number. |
| `next` | integer | Next page number. |
| `has_prev` | boolean | Whether a previous page exists. |
| `has_next` | boolean | Whether a next page exists. |
| `prev_url` | string | URL calculated for the previous page number. |
| `next_url` | string | URL calculated for the next page number. |

`prev_url` and `next_url` are always present; check `has_prev` or `has_next` before rendering their links.
