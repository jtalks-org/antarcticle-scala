package conf

import javax.naming.{Context, InitialContext}
import scala.reflect.runtime.universe._
import scala.util.Try
import conf.Keys._
import com.typesafe.config.Config

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
    case UseFakeAuthentication => "ANTARCTICLE_USE_FAKE_AUTHENTICATION"
    case MailSmtpHost => "ANTARCTICLE_SMTP_HOST"
    case MailSmtpPort => "ANTARCTICLE_SMTP_PORT"
    case MailSmtpFrom => "ANTARCTICLE_SMTP_USER"
    case MailSmtpPassword => "ANTARCTICLE_SMTP_PASSWORD"
    case MailSmtpAuth => "ANTARCTICLE_SMTP_AUTH"
    case MailSmtpSsl => "ANTARCTICLE_SMTP_SSL"
    case k => throw new RuntimeException(s"Key $k can't be translated to JNDI property key")
  }
}

class TypesafeConfigPropertiesProvider(config: Config) extends PropertiesProvider {
  def apply[T : TypeTag](key: ConfigurationKey): Option[T] = {
    val translatedKey = translateKey(key)
    typeOf[T] match {
      //TODO: remove duplication
      case t if t =:= typeOf[String] =>
        Try(config.getString(translatedKey).asInstanceOf[T]).toOption
      case t if t =:= typeOf[Boolean] =>
        Try(config.getBoolean(translatedKey).asInstanceOf[T]).toOption
      case ut =>
        throw new RuntimeException(s"Type $ut is unsupported by Typesafe config provider.")
    }
  }

  // if config object exists, then most likely it is available
  def isAvailable = true

  private def translateKey(key: ConfigurationKey) = key match {
    case DbDriver   => "db.default.driver"
    case DbUrl      => "db.default.url"
    case DbUser     => "db.default.user"
    case DbPassword => "db.default.password"
    case PoulpeUrl  => "security.authentication.poulpe.url"
    case UseFakeAuthentication => "security.authentication.useFake"
    case MailSmtpHost => "mail.smtp.host"
    case MailSmtpPort => "mail.smtp.port"
    case MailSmtpFrom => "mail.smtp.user"
    case MailSmtpPassword => "mail.smtp.password"
    case MailSmtpAuth => "mail.smtp.auth"
    case MailSmtpSsl => "mail.smtp.ssl"

    case k => throw new RuntimeException(s"Key $k can't be translated to Typesafe config key")
  }
}
