package util

import conf.PropertiesProviderComponent
import conf.Keys._

trait FakePropertiesProvider extends  PropertiesProviderComponent {
  override val propertiesProvider = {
    new conf.PropertiesProvider {
      override def apply[T](property: ConfigurationKey[T]): Option[T] = None
      override def isAvailable = true
    }
  }
}
