
# File Layout



```bash

# content
content/
    index.md            <-- index page
    posts               <-- here the posts live
        index.md        <-- index page for posts
        mypost.md       <-- one post
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
    default.peb     <-- default layout: for index page, about page etc
    post.peb        <-- layout for posts
_includes/          <-- snippets/fragments/helpers
    header.peb      <-- header snippet
    footer.peb      <-- footer snippet
    search.peb      <-- search form snippet
    pagination.peb  <-- pagination snippet
_site/              <-- result of rendering, this will be deployed
.flatmark-cache     <-- cache for the flatmark results, can be deleted
```

The `index.md` pages are special.
They get the list of all posts, so you can list them there in `index.md` pages.

