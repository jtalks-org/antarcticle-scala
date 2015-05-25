package conf

import scala.reflect.runtime.universe._

object Keys extends Enumeration {
  protected case class Val[T: TypeTag](jndi:String, prop: String, required: Boolean, default: Option[T] = None) extends super.Val
  type ConfigurationKey[T] = Val[T]
  val DbDriver = Val[String]("ANTARCTICLE_DB_DRIVER", "db.default.driver", required = true)
  val DbUrl = Val[String]("ANTARCTICLE_DB_URL", "db.default.url", required = true)
  val DbUser = Val[String]("ANTARCTICLE_DB_USER", "db.default.user", required = true)
  val DbPassword = Val[String]("ANTARCTICLE_DB_PASSWORD", "db.default.password", required = true)
  val PoulpeUrl = Val[String]("ANTARCTICLE_POULPE_URL", "security.authentication.poulpe.url", required = false)
  val MailSmtpSsl = Val[String]("ANTARCTICLE_SMTP_SSL", "mail.smtp.ssl", required = false, default = Some("true"))
  val MailSmtpHost = Val[String]("ANTARCTICLE_SMTP_HOST", "mail.smtp.host", required = true)
  val MailSmtpPort = Val[String]("ANTARCTICLE_SMTP_PORT", "mail.smtp.port", required = true)
  val MailSmtpUser = Val[String]("ANTARCTICLE_SMTP_USER", "mail.smtp.user", required = true)
  val MailSmtpFrom = Val[String]("ANTARCTICLE_SMTP_FROM", "mail.smtp.from", required = false)
  val MailSmtpPassword = Val[String]("ANTARCTICLE_SMTP_PASSWORD", "mail.smtp.password", required = true)
  val PoulpeUsername = Val[String]("ANTARCTICLE_POULPE_USERNAME", "security.authentication.poulpe.username", required = false)
  val PoulpeSecret = Val[String]("ANTARCTICLE_POULPE_PASSWORD", "security.authentication.poulpe.secret", required = false)
  val UseFakeAuthentication = Val[Boolean]("ANTARCTICLE_USE_FAKE_AUTHENTICATION", "security.authentication.useFake", required = false, default = Some(false))
  val DeleteInactiveUsers = Val[Boolean]("ANTARCTICLE_DELETE_INACTIVE_USERS", "security.delete.inactive.users", required = false, default = Some(false))
  val MailSmtpAuth = Val[Boolean]("ANTARCTICLE_SMTP_AUTH", "mail.smtp.auth", required = false, default = Some(false))

  def keyIterator: Iterator[ConfigurationKey[_]] = super.values.iterator.map(key => key.asInstanceOf[ConfigurationKey[_]])

}
