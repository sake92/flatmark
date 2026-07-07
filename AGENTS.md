# AGENTS.md — Flatmark

## Build & Test

- **Build tool:** Mill `0.12.14` (use `./mill`, not `mill`)
- **Scala:** `3.7.0`
- **Formatter:** scalafmt `3.7.15`, Scala 3 dialect, 120 cols
  - Run: `./mill -i mill.scalalib.scalafmt.ScalafmtModule/reformatAll __`
  - Verify: `./mill -i mill.scalalib.scalafmt.ScalafmtModule/checkFormatAll __`

### Common commands

| Task | Command |
|------|---------|
| Run all tests | `./mill -i __.test` |
| Run one test suite | `./mill -i ssr.test.testOnly ba.sake.flatmark.ssr.CodeHighlighterSuite` |
| Fat JAR | `./mill -i cli.jvm.assembly` (output: `out/cli/jvm/assembly.dest/out.jar`) |
| Run CLI | `./mill -i cli.jvm.run build -i docs` |
| Native installer (deb/pkg/msi) | `./mill -i show cli.<ubuntu\|macos\|windows>.jpackageAppImage2` |

### Formatting before commit

Always format before committing. The CI doesn't enforce it (no format check), but the convention is to format.

## Architecture

```
cli  ──depends-on──>  core ──depends-on──>  ssr
  │                     (Jinja, CommonMark)   (Selenium/Chrome headless)
  └──depends-on──>  swebserver
                      (Undertow + file-watch + WebSocket live-reload)
```

- **`cli/`** — CLI entrypoint (`ba.sake.flatmark.cli.Main`). Subcommands: `build` (generate site), `serve` (dev server with live-reload), `version`.
- **`core/`** — Site generation engine. Parses markdown (CommonMark), renders templates (Jinja), resolves themes, generates search index.
- **`ssr/`** — Server-side rendering via headless browser. Code highlighting (highlight.js), math (KaTeX), Mermaid/Graphviz diagrams. **Requires Chrome + Selenium.**
- **`swebserver/`** — Standalone static file server with WebSocket live-reload (separate package: `ba.sake.swebserver`).
- **`ssr-bundles/`** — JS bundles for the SSR browser. Built with **Bun** (`bun build index.mjs --outdir .`).

## Testing

- **Framework:** MUnit
- **Tests exist ONLY in `ssr/test/`** — there are no tests in `cli/` or `core/`.
- SSR tests render content through a headless browser (Selenium/Chrome) — they are **integration tests**, not unit tests.
- Run: `./mill -i __.test` or target a single suite with `ssr.test.testOnly <FQCN>`.

## Key constraints

- **Selenium/Chrome required for SSR at runtime** — code highlighting, diagrams, and math all go through a headless browser. The `ssr` module manages the WebDriver lifecycle.
- **Themes are downloaded from git at build/run time** (see `ThemeResolver.scala`). Results are cached in `.flatmark-cache/`.
- **File system layout == URL structure** — the tool maps directory structure directly to URLs.
- **No plugin system** — everything is built into a single fat JAR or native installer.
- **jpackage** is used to create native installers (deb/pkg/msi) via the custom `CustomJpackageModule` trait in `build.mill`.
- **Releases** use jreleaser. The mac installer version is hardcoded to `1.0.0` (Apple's pkg format requires `1.x.y+`). See `jreleaser.yml`.

## Environment variables

| Variable | Purpose |
|----------|---------|
| `VERSION` | Sets the version in the JAR manifest and native installer metadata |
| `FLATMARK_BASE_URL` | Base URL for the generated site (used when dogfooding docs) |

## Docs & examples

- **`docs/`** is itself a Flatmark site (the tool dogfoods itself). To build: `./mill -i cli.jvm.run build -i docs`
- **`examples/`** has 6 sample projects: `simple`, `multilang`, `local-theme`, `data-file`, `minimal`, `empty`.
- **`DESIGN.md`** covers the architectural rationale. **`DEV.md`** has GraalVM native-image metadata generation steps. **`TODO.md`** is the informal roadmap.

## Code style

- Package root: `ba.sake.flatmark` (CLI-only classes in `ba.sake.flatmark.cli`, SSR classes in `ba.sake.flatmark.ssr`)
- Exception: `swebserver` uses package `ba.sake.swebserver` (independent of flatmark)
- YAML config uses **snake_case** keys (e.g., `base_url`), deserialized via `scala-yaml` + custom readers in `YamlInstances.scala`
- Slf4j with `jdk14` provider in core, `simple` in test
