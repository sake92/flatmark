---
title: Themes
---

# Themes

By default, Flatmark uses the `default` theme, downloaded from [flatmark-themes repository](https://github.com/sake92/flatmark-themes).  
It is based on [PicoCSS](https://picocss.com/docs/conditional).

You can use a different theme by setting it in the `_config.yaml` file:

```yaml
theme:
  source: my_local_theme
# or
theme:
  source: https://github.com/my_user/my_repo?branch=main&folder=my_folder
```

In the first case, the theme is taken from the local `_themes/my_local_theme` folder.  

In the second case, it is downloaded from the GitHub repository,
from a specific branch (default is `main`),
and a specific folder (default is repo root `/`).

---
The folders read from theme are: `static`, `_layouts`, `_includes`, `_i18n`

You can override any file in those folders by mirroring them in your site folder.  
For example, if you want to override the `default.peb` layout, you can create a `_layouts/default.peb` file in your site folder.



