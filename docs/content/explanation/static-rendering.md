---
title: Why Flatmark renders dynamic-looking content statically
description: How build-time browser rendering shapes Flatmark sites
---

# {{page.title}}

Static site generators usually turn Markdown into HTML while leaving richer features—syntax highlighting, equations,
or diagrams—to JavaScript in the reader's browser. Flatmark moves that work into the build instead.

This choice extends the meaning of “static.” A Flatmark deployment consists of files that a basic web server or object
store can return as-is. Code is already highlighted, KaTeX has already rendered the equations, and Mermaid or Graphviz
has already produced SVG. The reader does not need those rendering libraries to execute successfully.

## A browser as a build tool

Those libraries are designed for a browser environment, so Flatmark runs a private local rendering service and drives
headless Chrome with Selenium during generation. Markdown fenced blocks are sent to the appropriate renderer and the
returned HTML or SVG is inserted into the page.

Using a browser at build time favors fidelity over a smaller toolchain. Flatmark can use the browser-oriented libraries
directly, but a build may need to provision a compatible Chrome runtime and is heavier than a plain Markdown-to-HTML
conversion.

## Caching changes the cost

Rendered fragments are keyed and stored in `.flatmark-cache/cached-results/`. Unchanged code, math, and diagrams can
reuse those results on later builds. The first build pays the setup and rendering cost; subsequent builds usually do
less browser work.

The cache is disposable. Removing it or using `--no-cache` changes build performance, not the meaning of the generated
site.

## Themes are build inputs

The same static-first idea applies to themes. Flatmark resolves a local theme or downloads a Git-hosted theme, then
combines its layouts, includes, translations, and static assets with site-specific overrides. The result is copied or
rendered into `_site/`; the theme repository is not required at deployment time.

This makes a theme part of the build's inputs rather than a runtime dependency. Remote themes are cached, and
`--update-theme` explicitly refreshes an existing cached checkout.

## Transactional output

A generated site should remain deployable even when a rebuild fails. Flatmark renders into a staging location and only
replaces `_site/` after generation succeeds. If Markdown, configuration, a template, or browser rendering fails, the
previous successful output remains available.

Together, build-time rendering, cached inputs, and transactional publication concentrate complexity in the authoring
environment. The deployed site stays deliberately simple.
