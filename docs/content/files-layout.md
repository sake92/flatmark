
# File Layout

```bash

# content
content/
    index.md            <-- index page
    blog                  <-- the blog category
        index.md            <-- index page for blog posts
        mypost.md           <-- one blog post
    docs                <-- the docs category
        index.md          <-- index page for docs
        mypost.md         <-- one doc page
    404.md              <-- 404 not found page
    bs/                 <-- translations live in lang-code/ named folders
        index.md
        blog/mypost.md    <-- translated post

static/                 <-- static files, copied as-is to the output
    images/favicon.ico
    styles/main.css
    scripts/main.js

# config
_config.yaml        <-- global config
_layouts/           <-- templates
    default.peb       <-- default layout: for index page, about page etc
    post.peb          <-- layout for posts
_includes/          <-- snippets/fragments/helpers
    header.peb        <-- header snippet
    footer.peb        <-- footer snippet
    search.peb        <-- search form snippet
    pagination.peb    <-- pagination snippet
_site/              <-- result of rendering, this will be deployed
.flatmark-cache     <-- cache for the flatmark results, can be deleted
```

The `index.md` (or `index.html`) pages are special.
They get the list of all pages in the category in the `paginator` argument, so you can list those items there.
For example, the `content/blog/index.md` would receive `paginator` with all the blog posts.
