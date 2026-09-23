# Flatmark

Flatmark turns a folder of Markdown, templates, and static assets into a static website.

[Documentation](https://sake92.github.io/flatmark/) · [Releases](https://github.com/sake92/flatmark/releases) ·
[Examples](examples) · [MIT license](LICENSE)

Flatmark renders syntax highlighting, KaTeX, Mermaid, and Graphviz during the build, so the generated site does not
need client-side JavaScript for them. It also provides Jinja templates, themes, multilingual routes, YAML data files,
search indexing, Sass compilation, and a development server with live reload.

## Quick start

For Debian or Ubuntu, download and install the `.deb` package:

```shell
curl -fL https://github.com/sake92/flatmark/releases/download/v0.2.0/flatmark_0.2.0_amd64.deb -o flatmark.deb
sudo apt install ./flatmark.deb
```

For macOS, other Linux distributions, and Windows, download the installer for your platform from the
[latest release](https://github.com/sake92/flatmark/releases/latest).

Create a site containing one Markdown page:

```text
my-site/
└── content/
    └── index.md
```

Add a heading to `content/index.md`:

```markdown
# Hello, Flatmark!
```

Then start the development server from `my-site/`:

```shell
flatmark serve
```

Open <http://localhost:5555>. Flatmark rebuilds the site and reloads the browser when a source file changes. Generated
files are written to `_site/`.

The first build may download the default theme and a headless Chrome runtime. Continue with the
[full quickstart](https://sake92.github.io/flatmark/tutorials/quickstart.html), or consult the
[CLI reference](https://sake92.github.io/flatmark/reference/cli.html).

## Documentation

- [Tutorials](https://sake92.github.io/flatmark/tutorials/) provide guided learning paths.
- [How-to guides](https://sake92.github.io/flatmark/howtos/) solve specific authoring and configuration tasks.
- [Reference](https://sake92.github.io/flatmark/reference/) documents configuration, templates, Markdown, and the CLI.
- [Explanation](https://sake92.github.io/flatmark/explanation/) describes Flatmark's build model and design trade-offs.

## Development

Flatmark uses Mill 0.12.14 and Scala 3.7.0.

```shell
./mill -i __.test
./mill -i cli.jvm.assembly
./mill -i cli.jvm.run build -i docs
```

See [DESIGN.md](DESIGN.md) for the module architecture and [DEV.md](DEV.md) for native-image development notes.
