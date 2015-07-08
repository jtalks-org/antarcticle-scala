package services

import javax.mail.internet.InternetAddress

import conf.Keys
import conf.{JndiPropertiesProvider, PropertiesProviderComponent}

import scala.util.{Failure, Success, Try}

trait ApplicationValidator {
  def validateApp: List[String]
}

trait ApplicationValidatorImpl extends ApplicationValidator {
  this: PropertiesProviderComponent =>


  override def validateApp: List[String] = {
    Keys.keyIterator.foldRight(List[String]()) { (key, list) =>
      def propName = if (propertiesProvider.isInstanceOf[JndiPropertiesProvider]) key.jndi else key.prop
      propertiesProvider.get(key) match {
        case Some(x) => key match {
          case Keys.MailSmtpFrom => Try {
            new InternetAddress(x.asInstanceOf[String])
          } match {
            case Success(_) => list
            case Failure(e) => s"$propName is not properly configured ${e.getMessage}" :: list
          }
          case _ => list
        }
        case None => if (key.required) {s"$propName is mandatory property" :: list} else list
      }
    }
  }
}