---
title: Quickstart
---

# {{page.title}}

## Installation
Go to the [releases page](https://github.com/sake92/flatmark/releases)
and download the installer for your platform.

```shell
user$ flatmark --version
Flatmark CLI 1.2.3
```


## Usage

Make a new folder for your site, e.g. `my_site`.  
Then create a new folder in it, called `content`.
Finally inside `content`, create a new file called `index.md` with the following content:

```markdown
# Welcome to Flatmark!
```

The structure of your site should look like this:

```
my_site/
├─ content/
│  ├─ index.md
```

Now you can run the Flatmark CLI to generate and serve your site locally.
Open a terminal, navigate to your `my:site` folder, and run the following command:

```shell
user$ flatmark serve
```

Then open your browser and go to `http://localhost:5555`.  
You should see your site with the content of `index.md`.

When you make changes to any file, the site will automatically rebuild and reload in the browser.

