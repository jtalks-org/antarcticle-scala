package utils

import java.security.MessageDigest
import java.util.UUID

import scala.util.Random

// todo: make it implement a trait and mix in SecurityComponent for the sake of DI
object SecurityUtil {

  def generateRememberMeToken = UUID.randomUUID.toString
  
  def generateSalt = Random.alphanumeric.take(64).mkString

  def md5(str: String) = MessageDigest.getInstance("MD5").digest(str.getBytes("UTF-8")).map("%02x" format _).mkString

  def encodePassword(password: String, salt: Option[String]) =
    salt match {
      case Some(salt) => md5(s"$password{$salt}")
      case _ => md5(password)
    }

}
