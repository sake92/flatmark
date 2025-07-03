---
title: Multilingual Site
description: Flatmark Multilingual Site tutorial
---

# {{page.title}}

This tutorial builds on the [Quickstart](/tutorials/quickstart.html) tutorial 
and shows how to create a multilingual site with Flatmark.

We need to add a bit of configuration to our site.
Create a file named `_config.yml` in the root of your site folder (`my_site/`) with the following content:

```yaml
lang: en
```
This sets the default language of your site to English.
You can set it to any language code you prefer, such as `fr` for French or `de` for German.
For full reference of language codes and subcodes, see https://www.w3.org/International/articles/language-tags/

The `content/` root folder contains the pages for your default language.

To add another language, create a subfolder named `bs/` 
(or any other name for your second language) inside the `content/` folder.
The files structure is mirrored, so the files should be called the same in both languages.

This is how it should look like this:
```
my_site/
└── content/
    ├── index.md
    ├── about/
    │   └── index.md
    └── bs/
        ├── index.md
        └── about/
            └── index.md
```

The `bs/` files will be served at `http://localhost:5555/bs/` URL.

When you `flatmark serve` the site and open it,
you should see a language switcher in the header on the top right.

You can take a look at 
[the example](https://github.com/sake92/flatmark/tree/main/examples/multilang)
in GitHub for reference.


> Example site: https://sake.ba/

