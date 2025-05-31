
# File Layout



```bash

# content
content/
    index.md            <-- index page, can be index.html too
    posts/mypost.md     <-- here the posts live
    404.md              <-- 404 not found page
    bs/                 <-- translations live in lang-code/ named folders
        index.md
        posts/mypost.md <-- translated post
    # resources
    images/favicon.ico
    styles/main.css
    scripts/main.js

# config
_config.yaml        <-- global config
_layouts/           <-- templates
    default.html    <-- default layout: for index page, about page etc
    post.html       <-- layout for posts
_includes/          <-- snippets/fragments/helpers
    header.html     <-- header snippet
    footer.html     <-- footer snippet
    search.html     <-- search form snippet
    pagination.html <-- pagination snippet
_site/              <-- result of rendering, this will be deployed
.flatmark-cache     <-- cache for the flatmark results, can be deleted
```

