---
title: Data Files
description: Flatmark Data Files tutorial
---

# {{page.title}}

Data files are normal YAML files that you can use to store structured data for your site.
You can use them in your pages and layouts to generate dynamic content.

## Setting up data files

Data files are stored in the `_data` folder inside your site folder.
Create a file `authors.yaml` in the `_data` folder with the following content:

```yaml
- name: Sakib
  skills: [scala, java, python]
- name: Senjin
  skills: [scala, java, javascript]
```

## Using data files in pages

You can use data files in your pages by using the `site.data` variable.



For example, you can create a file `content/authors.md` with the following content:

```markdown
{% raw %}
{{'{%'}} for author in site.data.authors %}
- {{'{{'}} author.name }}, skills: {{'{{'}} author.skills|join(', ') }}
{{'{%'}} endfor %}
{% endraw %}
```

This `for` loop goes through the array defined in `authors.yaml` and generates a list of authors.
It is the same as if you wrote:

```markdown
- Sakib, skills: scala, java, python
- Senjin, skills: scala, java, javascript
```

You can take a look at 
[the example](https://github.com/sake92/flatmark/tree/main/examples/data-file)
in GitHub for reference.


