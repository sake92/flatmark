---
title: Flatmark documentation
description: Build static websites from Markdown, Jinja templates, and static assets
pagination:
  enabled: false
---

# Flatmark

Flatmark turns a folder of Markdown, templates, and static assets into a static website. Code highlighting, math, and
diagrams are rendered during the build, leaving deployable HTML, CSS, images, and other static files in `_site/`.

## Start here

New to Flatmark? Follow the [Quickstart](/tutorials/quickstart.html) to create and serve a small site.

Already working on a site? Choose the documentation that matches your task:

- [Tutorials](/tutorials/) — learn Flatmark by building a site step by step.
- [How-to guides](/howtos/) — complete a specific authoring or configuration task.
- [Reference](/reference/) — look up CLI options, configuration keys, file conventions, and template variables.
- [Explanation](/explanation/) — understand Flatmark's static-rendering model and its trade-offs.

## What Flatmark provides

- CommonMark with tables, footnotes, task lists, and other authoring extensions
- build-time syntax highlighting, KaTeX, Mermaid, and Graphviz rendering
- Jinja layouts, includes, data files, pagination, and theme overrides
- multilingual routes with translation-aware template context
- generated search indexes
- a local server with file watching and live reload

## Installation

Download the installer for macOS, Linux, or Windows from the
[Flatmark releases page](https://github.com/sake92/flatmark/releases) and install it for your platform.

The first build may download the default theme and a compatible headless Chrome runtime.
