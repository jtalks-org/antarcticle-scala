package conf

import javax.naming.{Context, InitialContext}
import scala.util.Try
import conf.Keys._
import com.typesafe.config.Config

trait PropertiesProvider {
  def apply[T](property: ConfigurationKey[T]): Option[T]
  def get[T](property: ConfigurationKey[T]): Option[T] = apply[T](property)
  def isAvailable: Boolean
}

class JndiPropertiesProvider extends PropertiesProvider{
  private lazy val ctx = new InitialContext()
  private lazy val env = ctx.lookup("java:comp/env").asInstanceOf[Context]

  def apply[T](key: ConfigurationKey[T]): Option[T] = {
    val value = Try(env.lookup(key.jndi).asInstanceOf[T]).toOption
    value match {
      case Some(_) => value
      case None => key.default
    }
  }

  // naive test by instantiating lazy val
  // exception will occur if context isn't available
  def isAvailable = Try(env.toString).isSuccess

}

class TypesafeConfigPropertiesProvider(config: Config) extends PropertiesProvider {
  def apply[T](key: ConfigurationKey[T]): Option[T] = {
      val value = Try(config.getValue(key.prop).unwrapped().asInstanceOf[T]).toOption
      value match {
        case Some(_) => value
        case None => key.default
      }
  }

  // if config object exists, then most likely it is available
  def isAvailable = true
}
