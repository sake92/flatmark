

- headings anchors




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




