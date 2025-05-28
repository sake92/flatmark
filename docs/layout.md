
# File Layout



```bash

# content
index.md
posts/mypost.md     <-- here the posts live
404.md              <-- 404 not found page
bs/                 <-- translations live in lang-code folders
    index.md
    posts/mypost.md

# resources
images/favicon.ico
styles/main.css
scripts/main.js

# config
_config.yaml        <-- global config
_layouts/           <-- templates
    default.html    <-- default layout: for index page, about page etc
    post.html       <-- layout for posts
_site/              <-- result of rendering, this will be deployed
```


