---
title: Home
description: Home page
pagination:
  enabled: false
---

# Flatmark Documentation

Flatmark tries to be a simple static site generator without too much configuration.  

It is based on [Jinjava](https://github.com/HubSpot/jinjava/) for templating and uses [CommonMark](https://commonmark.org/) for markdown rendering.

Most of rendering is done statically: code highlighting, math, diagrams, etc.  
The only thing you usually need to add for those is CSS.

## Markdown
Syntax is based on [commonmark standard](https://commonmark.org/help/) with some extensions:
- [tables](https://docs.github.com/en/get-started/writing-on-github/working-with-advanced-formatting/organizing-information-with-tables#creating-a-table)
- strikethrough with double tilde: `~~text~~`
- autolink, turns plain text links into links, no need for markdown angle brackets: `https://example.com`
- footnotes, use `[^1]` to create a footnote and `[1]: footnote text` to define it
- ins, use `++text++` to mark text as inserted (underlined)
- task-list-items, use `- [ ]` for unchecked and `- [x]` for checked items
- image-attributes, use `{width=100%}` to set image width, `{height=50px}` for height etc.
- heading-anchor, automatically adds ids to headings

You can [learn markdown in 60 seconds](https://commonmark.org/help//).

### Syntax Highlighting

To use code syntax highlighting, use the code block syntax with the language specified:

````markdown
```scala
val x = 5
```
````

Result:
```scala
val x = 5
```

Syntax highlighting is done via [highlight.js](https://highlightjs.org/).

### Math

To use math blocks, use the `math` code block syntax:
````markdown
```math
x = 5
```
````

Result:
```math
x = 5
```

For more details, see [KaTeX](https://katex.org/).

### Mermaid Diagrams

To create Mermaid diagrams, use the `diagram:mermaid` code block syntax:

````markdown
```diagram:mermaid
sequenceDiagram
        actor Alice
        actor Bob
        Alice->>Bob: Hi Bob
        Bob->>Alice: Hi Alice
```
````

Result:
```diagram:mermaid
sequenceDiagram
        actor Alice
        actor Bob
        Alice->>Bob: Hi Bob
        Bob->>Alice: Hi Alice
```

For more details, see [Mermaid documentation](https://mermaid-js.github.io/mermaid/#/).

### Graphviz Diagrams

To create Graphviz diagrams, use the `diagram:graphviz` code block syntax:

````markdown
```diagram:graphviz
digraph G {Hello->World}
```
````


Result:
```diagram:graphviz
digraph G {Hello->World}
```

For more details, see [Graphviz documentation](https://graphviz.org/documentation/).





