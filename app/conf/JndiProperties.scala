package conf

import javax.naming.{Context, InitialContext}
import scala.reflect.runtime.universe._
import scala.util.Try
import conf.Keys._
import com.typesafe.config.Config

trait PropertiesProviderComponent  {
  // should be def to be independent of initialization order
  def propertiesProvider: PropertiesProvider
}

trait PropertiesProviderComponentImpl extends PropertiesProviderComponent {
  val propsProviderInstance = {
    def jndi = Option(new JndiPropertiesProvider).filter(_.isAvailable)
    def typesafe = Option(new TypesafeConfigPropertiesProvider(play.api.Play.current.configuration.underlying)).filter(_.isAvailable)
    def notFound = throw new RuntimeException("No available configuration providers")

    jndi orElse typesafe getOrElse notFound
  }

  // should be def to be independent of initialization order
  def propertiesProvider: PropertiesProvider = propsProviderInstance // singleton
}

trait PropertiesProvider {
  def apply[T : TypeTag](property: ConfigurationKey): Option[T]
  def get[T : TypeTag](property: ConfigurationKey): Option[T] = apply[T](property)
  def isAvailable: Boolean
}

class JndiPropertiesProvider extends PropertiesProvider{
  private lazy val ctx = new InitialContext()
  private lazy val env = ctx.lookup("java:comp/env").asInstanceOf[Context]

  def apply[T : TypeTag](key: ConfigurationKey): Option[T] = {
    val translatedKey = translateKey(key)
    Try(env.lookup(translatedKey).asInstanceOf[T]).toOption
  }

  // naive test by instantiating lazy val
  // exception will occur if context isn't available
  def isAvailable = Try(env.toString).isSuccess

  private def translateKey(key: ConfigurationKey) = key match {
    case DbDriver   => "ANTARCTICLE_DB_DRIVER"
    case DbUrl      => "ANTARCTICLE_DB_URL"
    case DbUser     => "ANTARCTICLE_DB_USER"
    case DbPassword => "ANTARCTICLE_DB_PASSWORD"
    case PoulpeUrl  => "ANTARCTICLE_POULPE_URL"
    case k => throw new RuntimeException(s"Key $k can't be translated to JNDI property key")
  }
}

class TypesafeConfigPropertiesProvider(config: Config) extends PropertiesProvider {
  def apply[T : TypeTag](key: ConfigurationKey): Option[T] = {
    val translatedKey = translateKey(key)
    typeOf[T] match {
      case t if t =:= typeOf[String] =>
        Try(config.getString(translatedKey).asInstanceOf[T]).toOption
      case ut =>
        throw new RuntimeException(s"Unsupported by Typesafe config properties provider type: $ut")
    }
  }

  // if config object exists, then most likely it is available
  def isAvailable = true

  private def translateKey(key: ConfigurationKey) = key match {
    case DbDriver   => "db.default.driver"
    case DbUrl      => "db.default.url"
    case DbUser     => "db.default.user"
    case DbPassword => "db.default.password"
    case PoulpeUrl  => "poulpe.url"
    case k => throw new RuntimeException(s"Key $k can't be translated to Typesafe config key")
  }
}
