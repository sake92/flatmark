package ba.sake.flatmark

import java.security.MessageDigest
import java.util.Base64

object HashUtils {
  
  def generate(str: String): String = {
    val bytesOfMessage = str.getBytes("UTF-8")
    val md = MessageDigest.getInstance("MD5")
    val theMD5digest = md.digest(bytesOfMessage)
    val b64 = Base64.getEncoder.encode(theMD5digest)
    val encoded = new String(b64, "UTF-8")
    
    // Use StringBuilder for efficient string manipulation
    val result = new StringBuilder(encoded.length)
    var i = 0
    while (i < encoded.length) {
      encoded.charAt(i) match {
        case '/' => result.append('-')
        case '=' => result.append('_')
        case '+' => result.append('$')
        case c => result.append(c)
      }
      i += 1
    }
    result.toString
  }
}
