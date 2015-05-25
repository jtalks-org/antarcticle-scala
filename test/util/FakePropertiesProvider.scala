package util

import conf.PropertiesProviderComponent
import conf.Keys._
import scala.reflect.runtime.universe._

trait FakePropertiesProvider extends  PropertiesProviderComponent {
  override val propertiesProvider = {
    new conf.PropertiesProvider {
      override def apply[T: TypeTag](property: ConfigurationKey[T]): Option[T] = None
      override def isAvailable = true
    }
  }
}
