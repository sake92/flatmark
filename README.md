
# Flatmark

## Design goals

Minimal setup:
- one command to install
- no dependencies


## Templating

https://github.com/jknack/handlebars.java/tree/master

https://jekyllrb.com/docs/variables/



## Markdown

U Javi, jer npr ne radi mermaidjs u node-u jer treba browser.
A imaju cli, tako da ne mogu bundle-at i cli unutar JS minified skripte, niđe veze.
Ovako iz jave more se shellout šta hoš.

const counter = mermaidCounter++;
            const mermaidInputFileName = `${siteFolder}/tmp/mermaid/diagram-${counter}.mmd`;
            const mermaidSvgFileName = `${siteFolder}/tmp/mermaid/diagram-${counter}.svg`;
            fs.writeFileSync(mermaidInputFileName, codeStr);
            const shell = process.platform === 'win32' ? 'powershell' : 'bash';
            execFileSync(
                './node_modules/.bin/mmdc',
                ['-i', mermaidInputFileName, '-o', mermaidSvgFileName, '-t', 'dark', '-b', 'transparent'],
                { shell: shell, encoding: 'utf-8' }
            );
            const svg = fs.readFileSync(mermaidSvgFileName, 'utf8');
            return `<pre class="diagram-mermaid">${svg}</pre>`; // avoid <code> wrapper




## Packaging

Jpackage for building installers.

https://akman.github.io/jpackage-maven-plugin/jpackage-mojo.html


