package ba.sake.flatmark

import java.security.MessageDigest
import java.util.Base64

class FileCache(cacheFolder: os.Path, useCache: Boolean) {

  def cached(prefix: String, cacheKeyParts: String*)(code: => String): String = {
    val cacheKey = getMd5B64(cacheKeyParts.mkString("-"))
    val cachedResultFileName = cacheFolder / "cached-results" / s"${prefix}-${cacheKey}.txt"
    if useCache && os.exists(cachedResultFileName) then {
      os.read(cachedResultFileName)
    } else {
      val finalResult = code
      os.write.over(cachedResultFileName, finalResult, createFolders = true)
      finalResult
    }
  }
  
  private def getMd5B64(str: String): String = {
    val bytesOfMessage = str.getBytes("UTF-8")
    val md = MessageDigest.getInstance("MD5")
    val theMD5digest = md.digest(bytesOfMessage)
    val b64 = Base64.getEncoder.encode(theMD5digest)
    new String(b64, "UTF-8").replace('/', '-').replace('=', '_').replace('+', '$')
  }
}
