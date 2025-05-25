

File system layout mirrors URLs.

```bash

# config
_config.yaml        <-- global config
_layouts/           <-- templates
    default.html    <-- default layout: for index page, about page etc
    post.html       <-- layout for posts
_site/              <-- result of rendering, this will be deployed

# content
index.md
mypost.md
404.md              <-- 404 not found page
bs/                 <-- translations live in lang-code folders
    index.md
    mypost.md

# resources
images/favicon.ico
styles/main.css
scripts/main.js

```

## _config.yaml
title:
url: sake.ba
description: SEO
lang: en
github_handle:
twitter_handle:

It can only be in the root folder.


## Front matter

```yaml
title: Hello
date: 2025-01-01 11:11 +02:00
layout: post
tags: [scala, java]
publish: yes | no | hide # hide means published but not listed or searched, thus not google indexed

custom_variable: whatever
```


## templating
    layout: page (i.e. index or about) | post

Kontammm moždaaaa `_layouts/post.html` za layout "post".  #stonks
Jer mislio sam obične `*.html` fajlove dozvolit isto, YOLO.

includes
{{ include _header.html }}


https://jekyllrb.com/docs/variables/
kae site.nesto čita iz _config.yaml
a npr page.nesto iz stranice normala


## i18n

Just folders, but contextual.
E.g. u templatingu reć posts da bude vezan za taj bs/ folder samo !!!

Overrideat parent fajlove, tipa mypost.md u rootu overridaš sa bs/mypoost.md .
Npr moreš i about.md.
Isto i sa layoutima.

## static search

Kopirat sa hepeka..

## Data files
Koncept iz jekylla zgodan.
Tipa imaš _bla.yml ili _bla.json sa nekim data,
i onda moreš reć `site.bla . foreach ..`.

Zgodno kad imaš neki externi data koji hoćeš da metneš u sajt.


- synccc
    - delete old files
    - ignore _ and . files and .gitignored files


- virtual hosts i domene


## Dev loop
- `flatmark serve` to build, watch and serve the site
- it will show a label down below with a warning that this is a dev server
- it will render even the posts that are not published! (and show a nice warning)


## Migrating from other platforms
- import from wordpress, jekyll itd

- RSS




