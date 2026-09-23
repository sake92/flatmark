---
title: Markdown and rendering
description: Flatmark Markdown extensions and build-time rendering reference
---

# {{page.title}}

Flatmark parses [CommonMark](https://commonmark.org/) and enables the extensions below.

## Markdown extensions

| Feature | Syntax |
|---|---|
| Tables | GitHub-style pipe tables. |
| Strikethrough | `~~removed~~` |
| Autolinks | Plain HTTP and HTTPS URLs. |
| Footnotes | Reference with `[^1]`; definition with `[^1]: Text`. |
| Inserted text | `++inserted++` |
| Task-list items | `- [ ] Open` and `- [x] Complete` |
| Image attributes | `![Alt](image.png){width=100% height=50px}` |
| Heading anchors | `id` attributes generated from heading text. |

Flatmark appends a permalink with class `flatmark-anchor` to headings that have an `id`.

## Fenced code blocks

A fenced code block with a language identifier is syntax-highlighted during the build:

````markdown
```scala
val greeting = "Hello"
```
````

Highlighting uses highlight.js and emits HTML rather than browser-side highlighting code.

## Math

Inline math uses single dollar delimiters:

```markdown
The area is $A = \pi r^2$.
```

Block math uses a fenced block with the `math` identifier:

````markdown
```math
A = \pi r^2
```
````

Math is rendered with KaTeX during the build.

## Mermaid

Use the `diagram:mermaid` identifier:

````markdown
```diagram:mermaid
flowchart LR
    Markdown --> HTML
```
````

## Graphviz

Use the `diagram:graphviz` identifier:

````markdown
```diagram:graphviz
digraph G { Markdown -> HTML }
```
````

Mermaid and Graphviz diagrams are emitted as SVG. Highlighting, math, and diagram results are cached under
`.flatmark-cache/cached-results/` unless the CLI is run with `--no-cache`.
