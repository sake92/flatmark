---
title: Themes
description: Flatmark theme source and override reference
---

# {{page.title}}

Themes provide layouts, includes, translations, and static assets. Theme loading is controlled by the `theme` object in
`_config.yaml`.

## Configuration

| Key | Type | Default | Description |
|---|---|---|---|
| `theme.enabled` | boolean | `true` | Enables theme resolution and loading. |
| `theme.source` | string | Flatmark default-theme URL | Local theme name or HTTP(S) Git repository URL. |

## Local sources

A source without a URL scheme names a folder below `_themes/`:

```yaml
theme:
  source: my-theme
```

This resolves to `_themes/my-theme/`. A missing local theme folder fails the build.

## Remote sources

An HTTP or HTTPS source identifies a Git repository. The URL supports these query parameters:

| Parameter | Default | Description |
|---|---|---|
| `branch` | `main` | Branch passed to the initial shallow clone. |
| `folder` | `.` | Theme folder within the repository. |

```yaml
theme:
  source: https://github.com/example/site-themes?branch=stable&folder=minimal
```

Remote repositories are stored below `.flatmark-cache/themes/`. An existing checkout is reused unless the CLI receives
`--update-theme`, which runs `git pull` in that checkout.

Supported URL schemes are `http` and `https`.

## Theme folders

| Folder | Purpose |
|---|---|
| `_layouts/` | Page and index layouts. |
| `_includes/` | Reusable Jinja templates. |
| `_i18n/` | Translation resource bundles. |
| `static/` | Assets copied to the generated site. |

## Site overrides

Site-level `_layouts/` and `_includes/` are searched before their theme counterparts. A site layout or include with the
same relative path therefore overrides the theme file.

Theme static assets are copied first and site `static/` assets second. A site asset replaces a theme asset at the same
output path.
