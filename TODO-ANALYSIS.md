# TODO audit and prioritization

This records the review behind the ordering in [TODO.md](TODO.md). The audit compares the items with the repository as of 2026-09-23. The size labels in the TODO are rough estimates, not implementation plans.

## Removed as completed

| Original item | Evidence |
|---|---|
| Pagination docs | [Configuration](docs/content/reference/configuration.md) describes pagination settings, [template context](docs/content/reference/template-context.md) lists paginator fields, and [content how-to](docs/content/howtos/content.md) shows disabling pagination. |
| Template context variables | [Template context reference](docs/content/reference/template-context.md) documents `site`, `page`, and `paginator`. |
| Theme authoring docs | [Custom local theme tutorial](docs/content/tutorials/custom_local_theme.md) creates layouts and selects a theme; [themes reference](docs/content/reference/themes.md) covers sources, folders, and overrides. |
| CLI reference | [CLI reference](docs/content/reference/cli.md) lists commands, options, and exit behavior. |
| Categories docs | [Configuration](docs/content/reference/configuration.md), [file layout](docs/content/reference/files-layout.md), and [template context](docs/content/reference/template-context.md) describe category configuration, routing, and template data. |
| Sass support | [AssetPublisher.scala](core/src/ba/sake/flatmark/generation/AssetPublisher.scala) compiles `_sass/` into `_site/styles/`, and [FlatmarkGenerator.scala](core/src/ba/sake/flatmark/generation/FlatmarkGenerator.scala) invokes it during generation. |

## Kept or narrowed

- **Sass documentation:** The [file layout reference](docs/content/reference/files-layout.md) mentions `_sass/` and the required `sass` executable, but gives no setup or usage example. The remaining item asks for those details.
- **Tags:** [SiteConfig](core/src/ba/sake/flatmark/config.scala) accepts tag labels and descriptions, and [templateContext.scala](core/src/ba/sake/flatmark/templateContext.scala) exposes them. Pages have no tag assignment field or tag-based listing, so the feature remains incomplete.
- **Translation URL filter:** [SitePlanner.scala](core/src/ba/sake/flatmark/generation/SitePlanner.scala) resolves matching translated routes and [template context](docs/content/reference/template-context.md) documents `site.langs[].url`. [FlatmarkTemplateHandler.scala](core/src/ba/sake/flatmark/templates/FlatmarkTemplateHandler.scala) registers a Markdown filter, but no `translation_url` filter. This is lower priority because templates already have URLs for the current page's languages.
- **Publishing preview:** [PageConfig](core/src/ba/sake/flatmark/config.scala) has `publish_date`, but there is no draft or published field, date-based filtering, or serve-only preview behavior. This needs a defined publishing rule as part of implementation.
- **Active category link:** The default theme now marks the active category link with `aria-current="page"`, so this item was removed from the TODO.

## Priority rationale

The first two items address common site authoring needs: checking broken references before publishing and creating a new site quickly. A code copy button is a small improvement used across documentation pages. Draft and future-date preview, and social preview images, are useful publishing features but need more design and implementation work.

The remaining small tasks improve workflow or presentation. Tags, feeds, content widgets, and imports affect more of the content model or generated output, so they follow the focused changes. Heading-level checks are listed after the first `flatmark check` milestone so that command can ship with the highest-value checks first. Theme examples remain ideas rather than committed feature work.

The ordering is a judgment based on the current code and docs. It does not include user demand, usage data, or implementation spikes.
