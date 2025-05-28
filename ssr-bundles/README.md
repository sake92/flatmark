

## HighlightJs

http://localhost:8181/highlightjs?code=val%20x%20=%205&lang=scala

Bundled with `bun build .\index.mjs --outdir .`



## Katex

http://localhost:8181/katex?source=c%20%3D%20%5Cpm%5Csqrt%7Ba%5E2%20%2B%20b%5E2%7D

Bundle is from https://cdn.jsdelivr.net/npm/katex@0.16.22/dist/katex.mjs
just renamed to katex.js so that Undertow can serve it.



## Graphviz

http://localhost:8181/graphviz?source=digraph%20%7B%20a%20-%3E%20b%3B%20%7D&engine=neato

Bundled with `bun build .\index.mjs --outdir .`



## Mermaid

http://localhost:8181/mermaidjs?source=graph%20TD%0A%20%20%20%20%20%20%20%20%20%20%20%20A%5BClient%5D%20--%3E%20B%5BLoad%20Balancer%5D%0A%20%20%20%20%20%20%20%20%20%20%20%20B%20--%3E%20C%5BServer1%5D%0A%20%20%20%20%20%20%20%20%20%20%20%20B%20--%3E%20D%5BServer2%5D

Bundled with `bun build .\index.mjs --outdir .`
