package conf

object Keys {
  sealed trait ConfigurationKey
  case object DbDriver extends ConfigurationKey
  case object DbUrl extends ConfigurationKey
  case object DbUser extends ConfigurationKey
  case object DbPassword extends ConfigurationKey
  case object PoulpeUrl extends ConfigurationKey
  case object UseFakeAuthentication extends ConfigurationKey
  case object DeleteInacticveUsers extends ConfigurationKey
  case object MailSmtpAuth extends ConfigurationKey
  case object MailSmtpSsl extends ConfigurationKey
  case object MailSmtpHost extends ConfigurationKey
  case object MailSmtpPort extends ConfigurationKey
  case object MailSmtpFrom extends ConfigurationKey
  case object MailSmtpUser extends ConfigurationKey
  case object MailSmtpPassword extends ConfigurationKey
  case object PoulpeUsername extends ConfigurationKey
  case object PoulpeSecret extends ConfigurationKey
}
