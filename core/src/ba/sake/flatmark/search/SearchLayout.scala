package ba.sake.flatmark.search

import ba.sake.flatmark.SiteConfig

object SearchLayout {

  def build(siteConfig : SiteConfig): String =
    s"""
      |{% extends "base" %}
      |    
      |{% block title %}Search Results{% endblock %}
      |
      |{% block content %}
      |    <div id="search-results-content"></div>
      |{% endblock %}
      |
      |{% block scripts %}
      |<script type="module">
      |import Fuse from 'https://cdn.jsdelivr.net/npm/fuse.js@7.1.0/dist/fuse.mjs'
      |
      |const urlParams = new URLSearchParams(window.location.search);
      |const qParam = urlParams.get('q');
      |
      |const entries = await fetch('${siteConfig.base_url.getOrElse("")}/search/entries.json').then(r => r.json());
      |console.log("Entries:", entries);
      |
      |
      |const fuse = new Fuse(entries, {
      |  keys: [ "title", "text" ]
      |});
      |
      |const searchRes = fuse.search(qParam);
      |console.log("Search results for:", qParam, searchRes);
      |const searchResultsContentElem = document.getElementById("search-results-content");
      |if (searchRes.length === 0) {
      |    document.getElementById("search-results-content").innerHTML = `<p>No results found for <strong>$${qParam}</strong>.</p>`;
      |} else {
      |    searchResultsContentElem.innerHTML = searchRes.map(r => {
      |    const page = r.item;
      |    return `<div class="search-result-item">
      |        <h2><a href="$${page.url}">$${page.title}</a></h2>
      |        <p>$${page.text.substring(0, 200)}</p>
      |      </div>`;
      |    }).join("");
      |}
      |</script>
      |{% endblock %}
      |    
      |    
      |""".stripMargin
}
