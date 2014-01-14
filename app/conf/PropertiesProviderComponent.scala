package conf

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
