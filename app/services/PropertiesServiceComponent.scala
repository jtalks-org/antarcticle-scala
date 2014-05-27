package services

trait PropertiesServiceComponent {
  val propertiesService: PropertiesService

  trait PropertiesService {
    def getInstanceName(): String

    def changeInstanceName(newName: String)
  }
}

trait PropertiesServiceComponentImpl extends PropertiesServiceComponent {
  val propertiesService = new PropertiesServiceImpl

  class PropertiesServiceImpl extends PropertiesService {
    def getInstanceName(): String = {
      ""
    }

    def changeInstanceName(newName: String): Unit = {

    }
  }
}
