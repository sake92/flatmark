package ba.sake.flatmark

import java.security.MessageDigest
import java.util.Base64

object HashUtils {
  
  def generate(str: String): String = {
    val bytesOfMessage = str.getBytes("UTF-8")
    val md = MessageDigest.getInstance("MD5")
    val theMD5digest = md.digest(bytesOfMessage)
    val b64 = Base64.getEncoder.encode(theMD5digest)
    new String(b64, "UTF-8").replace('/', '-').replace('=', '_').replace('+', '$')
  }
}
