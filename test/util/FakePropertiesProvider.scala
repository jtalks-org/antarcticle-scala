package util

import conf.PropertiesProviderComponent

import scala.reflect.runtime.universe._

trait FakePropertiesProvider extends  PropertiesProviderComponent {
  override val propertiesProvider = {
    new conf.PropertiesProvider {
      override def apply[T: TypeTag](property: conf.Keys.ConfigurationKey): Option[T] = None
      override def isAvailable = true
    }
  }
}
