---
title: Themes
---

# Themes

By default, Flatmark uses the `default` theme, downloaded from [Flatmark repository](https://github.com/sake92/flatmark/tree/main/themes/default).  
It is based on [PicoCSS](https://picocss.com/docs/conditional).

You can use a different theme by setting it in the `_config.yaml` file:

```yaml
theme: my_local_theme
# or
theme: https://github.com/sake92/flatmark?branch=main&folder=a_folder
```

In the first case, the theme is taken from the local `_themes/my_local_theme` folder.  

In the second case, it is downloaded from the GitHub repository,
from a specific branch (default is `main`),
and a specific folder (default is repo root `/`).

---
The folders read from theme are: `static`, `_layouts`, `_includes`, `_i18n`

You can override any file in those folders by mirroring them in your site folder.  
For example, if you want to override the `default.peb` layout, you can create a `_layouts/default.peb` file in your site folder.



