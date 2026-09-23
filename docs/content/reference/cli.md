---
title: CLI
description: Flatmark command-line reference
---

# {{page.title}}

## Syntax

```text
flatmark [command] [options]
```

The default command is `build`.

## Commands

| Command | Description |
|---|---|
| `build` | Generate the site and exit. |
| `serve` | Generate the site, serve `_site/`, watch source files, and reload connected browsers after changes. |
| `version` | Print the version and exit. |

Unknown commands exit with status 1.

## Options

| Option | Default | Description |
|---|---:|---|
| `-v`, `--version` | off | Print the version and exit. |
| `-i`, `--input` | `.` | Site root folder. |
| `-h`, `--host` | `localhost` | Host interface used by `serve`. |
| `-p`, `--port` | `5555` | HTTP port used by `serve`. |
| `-l`, `--log-level` | `info` | Log level: `debug`, `info`, or `error`. |
| `--no-cache` | off | Skip cached server-side rendering results. |
| `--update-theme` | off | Pull the configured remote theme when it is already cached. |

`--host` and `--port` have no effect on the generated site produced by `build`.

## Environment variables

| Variable | Description |
|---|---|
| `FLATMARK_BASE_URL` | Supplies `base_url` when `_config.yaml` does not define it. |

## Exit behavior

`build` exits with status 1 when generation fails. `serve` remains active until interrupted and stops its HTTP and
server-side-rendering servers during shutdown.
