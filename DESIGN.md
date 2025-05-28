

Current design of the project:
- graalvm for native image
- serving syntax highlighting, diagram rendering trough http and getting it via Selenium
  - works even for mermaidjs, diagrams are plain SVGs
  - but we do need chrome installed!
- file cache for diagrams, code highlighting, etc.


## Modules

- `ssr-bundles` contains nodejs+bun logic for bundling the JS code needed for SSR, like syntax highlighting and diagram rendering
- `ssr` contains the server-side rendering logic, renders HTML pages with syntax highlighting and diagrams
  - the `core` is using `ssr` routes to render snippets of code and diagrams
- `core` contains the core logic of the application, like parsing markdown, templates, rendering diagrams via Selenium etc.
- `cli` contains the command line interface for the application



## GraalVM native image

Best for CLIs, snappiest startup time etc.
Need to be careful with reflection, dynamic class loading, etc.


## jpackage

Makes installers for all platforms.
Would be veery nice, easier to distribute to winget, apt, brew, etc.

