

Files layout mirrors URLs.

```bash
_config.yaml        <-- global config
index.md
mypost.md
404.md              <-- 404 not found page
images/favicon.ico
styles/main.css
scripts/main.js
bs/                 <-- translations live here, kao zasebna web stranica haman
    index.md
    mojpost.md

_site/              <- result of rendering, this will be deployed
```

`_config.yaml`
title:
url: sake.ba
description: SEO
lang: en
github_handle:
twitter_handle:
....


Možda dozvolit `_config.yaml` u BILO KOM folderu.
Tu metnut npr `lang: bs` da overridea.

I možda metnut isto kao Jekyll defaulte neke za layout, datum, naslov itd...
```yaml
defaults:
    draft: true
    layout: post
    tags: [scala, java]
```


## front matter
```yaml
draft: true (published but NOT listed or searched, thus not google indexed)
layout: post
title: Hello
date: 2025-01-01 11:11 +02:00
tags: [scala, java]

custom_variable: whatever
```

Draftove ne rendat nikako kad deployamo.
AAAAAAA, ima `jekyll serve --draft` npr, kad lokalno devaš.


## templating
    layout: page (i.e. index or about) | post

Kontammm moždaaaa `_layouts/post.html` za layout "post".  #stonks
Jer mislio sam obične `*.html` fajlove dozvolit isto, YOLO.
IIIIIII možda čak metnut `_layouts` u translation folder, što da ne.
Onda tražit samo u najbližem parentu `_layouts` i to je to.
Hmm, kae more i layout imat layout u Jekyllu, rekurzija... no bueno

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


- serve i watch ??

- import from wordpress, jekyll itd

- RSS




