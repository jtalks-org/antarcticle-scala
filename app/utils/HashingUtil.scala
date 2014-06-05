package utils

import java.security.MessageDigest

object HashingUtil {

  def md5(str: String) = MessageDigest.getInstance("MD5").digest(str.getBytes("UTF-8")).map("%02x" format _).mkString

}
