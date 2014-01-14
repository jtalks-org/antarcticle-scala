package conf

object Keys {
  sealed trait ConfigurationKey
  case object DbDriver extends ConfigurationKey
  case object DbUrl extends ConfigurationKey
  case object DbUser extends ConfigurationKey
  case object DbPassword extends ConfigurationKey
  case object PoulpeUrl extends ConfigurationKey
}
