---
title: Quickstart
description: Build and serve your first Flatmark site
---

# {{page.title}}

In this tutorial, you will create a one-page site, preview it with live reload, and produce a deployable build.

## Installation

On macOS, install Flatmark from the Homebrew tap:

```shell
brew tap sake92/tap
brew install --cask flatmark
```

On Debian or Ubuntu, download and install the `.deb` package:

```shell
curl -fL https://github.com/sake92/flatmark/releases/download/v0.2.0/flatmark_0.2.0_amd64.deb -o flatmark.deb
sudo apt install ./flatmark.deb
```

For other Linux distributions and Windows, go to the [releases page](https://github.com/sake92/flatmark/releases),
download the installer for your platform, and install it. Confirm the installation:

```shell
flatmark --version
```

The command prints the installed Flatmark version.

## Create the site

Create a folder named `my-site`, then create a `content` folder inside it:

```shell
mkdir -p my-site/content
cd my-site
```

Create `content/index.md` with this content:

```markdown
---
title: Home
description: My first Flatmark site
---

# Hello, Flatmark!

This page was generated from Markdown.
```

Your site now has this structure:

```text
my-site/
└── content/
    └── index.md
```

## Preview the site

Start the development server from the `my-site` folder:

```shell
flatmark serve
```

The first run may download the default theme and a compatible headless Chrome runtime. When the server reports that it
has started, open <http://localhost:5555>. You will see the heading and paragraph from `content/index.md`.

Change the heading in `content/index.md` and save the file. Flatmark rebuilds the site and reloads the page in your
browser.

Stop the server with <kbd>Ctrl</kbd>+<kbd>C</kbd>.

## Create a production build

Run:

```shell
flatmark build
```

Flatmark writes the generated site to `_site/`. The folder contains static files and can be deployed to any static
hosting service.

You now have a complete Flatmark build. Continue with the [multilingual site tutorial](/tutorials/multilang.html), or
look up the available commands in the [CLI reference](/reference/cli.html).
