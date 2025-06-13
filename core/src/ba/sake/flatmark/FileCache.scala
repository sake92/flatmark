package ba.sake.flatmark


class FileCache(cacheFolder: os.Path, useCache: Boolean) {

  def cached(prefix: String, cacheKeyParts: String*)(code: => String): String = {
    val cacheKey = HashUtils.generate(cacheKeyParts.mkString("-"))
    val cachedResultFileName = cacheFolder / "cached-results" / s"${prefix}-${cacheKey}.txt"
    if useCache && os.exists(cachedResultFileName) then {
      os.read(cachedResultFileName)
    } else {
      val finalResult = code
      os.write.over(cachedResultFileName, finalResult, createFolders = true)
      finalResult
    }
  }
}
