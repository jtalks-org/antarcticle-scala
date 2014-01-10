package conf

import javax.naming.{Context, InitialContext}
import scala.util.Try

trait PropertiesProviderComponent  {
  // should be def to be independent of initialization order
  def propertiesProvider: PropertiesProvider

  trait PropertiesProvider {
    def apply[T](property: String): Option[T]
    def get[T](property: String): Option[T] = apply[T](property)
  }
}

trait JndiPropertiesProviderComponent extends PropertiesProviderComponent {
  lazy val pp = new JndiPropertiesProvider
  def propertiesProvider = pp // singletone

  class JndiPropertiesProvider extends PropertiesProvider{
    private lazy val ctx = new InitialContext()
    private lazy val env = ctx.lookup("java:comp/env").asInstanceOf[Context]

    def apply[T](resource: String): Option[T] =
      Try(env.lookup(resource).asInstanceOf[T]).toOption
  }
}

