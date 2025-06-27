package ba.sake.flatmark.search

import ba.sake.tupson.JsonRW

case class SearchEntry(
    title: String,
    url: String,
    text: String
) derives JsonRW
