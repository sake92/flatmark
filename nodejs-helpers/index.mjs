import hljs from 'highlight.js';
import katex from 'katex';
import { Graphviz } from "@hpcc-js/wasm-graphviz";

export function highlightCode(codeStr, lang) {
    return hljs.highlight(codeStr, { language: lang }).value;
}
export function highlightCodeAuto(codeStr) {
    return hljs.highlightAuto(codeStr).value;
}


export function highlightMath(katexStr) {
    return katex.renderToString(katexStr, {
        throwOnError: false
    });
}


const graphviz = await Graphviz.load();
export function renderGraphviz(dotStr, engine) {
    return graphviz.layout(dotStr, 'svg', engine);
}
