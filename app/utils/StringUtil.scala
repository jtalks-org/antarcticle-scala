package utils

import java.security.MessageDigest

object StringUtil {

  def generateMd5Hash(str: String) = {
    MessageDigest.getInstance("MD5").digest(str.getBytes("UTF-8")).map("%02X" format _).mkString.toLowerCase
  }

}
