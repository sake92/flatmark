

Current design of the project:
- one executable, no plugins
- http server + Selenium for syntax highlighting, diagram rendering etc (everything that needs a browser)
  - works even for mermaidjs, diagrams are plain SVGs!
  - file cached
  - selenium handles chrome installation for us
- file system layout == URLs



## Modules

- `ssr-bundles` contains nodejs+bun logic for bundling the JS code needed for SSR, like syntax highlighting and diagram rendering
- `ssr` contains the server-side rendering logic, renders HTML pages with syntax highlighting and diagrams
  - the `core` is using `ssr` routes to render snippets of code and diagrams
- `core` contains the site-generation pipeline:
  - `SiteConfigLoader` and `SiteSourceLoader` validate configuration and source files at the boundary
  - `SitePlanner` derives routes, languages, categories, sorting, and pagination without writing output
  - `PageRenderer` renders planned pages through Jinja, Markdown, and Jsoup
  - `AssetPublisher` publishes static assets and compiles Sass
  - completed builds replace `_site` transactionally; failed builds leave the previous site intact
- `cli` contains the command line interface for the application
- `swebserver` contains the static files webserver with live reload



## GraalVM Native Image

- one executable for the whole application
- snappy startup time
- minimal memory usage
- minimum app size

Need to be careful with reflection, dynamic class loading, etc.

