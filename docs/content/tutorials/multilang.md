---
title: Multilingual site
description: Add a second language to a Flatmark site
---

# {{page.title}}

In this tutorial, you will add a Bosnian translation to the site from the
[Quickstart](/tutorials/quickstart.html).

## Set the default language

Create `_config.yaml` in the site root:

```yaml
lang: en
```

Files directly inside `content/` now represent the English site.

## Add the translated home page

Create `content/bs/index.md`:

```markdown
---
title: Početna
description: Moja prva Flatmark stranica
---

# Zdravo, Flatmark!

Ova stranica je generisana iz Markdown dokumenta.
```

The site has this structure:

```text
my-site/
├── _config.yaml
└── content/
    ├── index.md
    └── bs/
        └── index.md
```

## Preview both languages

Run:

```shell
flatmark serve
```

Open <http://localhost:5555> for the English page and <http://localhost:5555/bs/> for the Bosnian page. The default
theme displays a language switcher linking the two versions.

## Add matching pages

Create `content/about.md`:

```markdown
# About
```

Then create its translation at `content/bs/about.md`:

```markdown
# O nama
```

Open <http://localhost:5555/about.html> and use the language switcher. It links to `/bs/about.html` because Flatmark
matches translations by their path relative to the default-language or language-code folder.

The completed structure matches the
[multilingual example](https://github.com/sake92/flatmark/tree/main/examples/multilang).
