

Current design of the project:
- one executable, no plugins
- http server + Selenium for syntax highlighting, diagram rendering etc (everything that needs a browser)
  - works even for mermaidjs, diagrams are plain SVGs!
  - we do need chrome installed tho
  - file cached
- file system layout == URLs



## Modules

- `ssr-bundles` contains nodejs+bun logic for bundling the JS code needed for SSR, like syntax highlighting and diagram rendering
- `ssr` contains the server-side rendering logic, renders HTML pages with syntax highlighting and diagrams
  - the `core` is using `ssr` routes to render snippets of code and diagrams
- `core` contains the core logic of the application, like parsing markdown, templates, rendering diagrams via Selenium etc.
- `cli` contains the command line interface for the application



## GraalVM Native Image

- one executable for the whole application
- snappy startup time
- minimal memory usage
- minimum app size

Need to be careful with reflection, dynamic class loading, etc.


