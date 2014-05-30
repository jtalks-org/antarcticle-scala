package services

import repositories.PropertiesRepositoryComponent

trait PropertiesServiceComponent {
  val propertiesService: PropertiesService

  trait PropertiesService {
    def getInstanceName(): String

    def changeInstanceName(newName: String)
  }
}

trait PropertiesServiceComponentImpl extends PropertiesServiceComponent {
  this: SessionProvider with PropertiesRepositoryComponent =>

  val propertiesService = new PropertiesServiceImpl

  class PropertiesServiceImpl extends PropertiesService {

    def getInstanceName(): String = withSession {
      implicit session =>
        val propertyName = "INSTANCE_NAME"
        val defaultValue = "ANTARCTICLE"
        val property = propertiesRepository.getProperty(propertyName)

        property match {
          case Some(x) =>
            x.value.getOrElse(defaultValue)
          case None => defaultValue
        }
    }

    def changeInstanceName(newName: String): Unit = withSession {
      implicit session =>
        val propertyName = "INSTANCE_NAME"
        propertiesRepository.changeProperty(propertyName, Some(newName))
    }
  }
}
