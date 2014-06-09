package services

import repositories.PropertiesRepositoryComponent
import security.{Entities, Principal}
import security.Permissions.{Manage, Create}
import security.Result.AuthorizationResult


object PropertyNames {
  val instanceNameProperty = "INSTANCE_NAME"
}

trait PropertiesServiceComponent {
  val propertiesService: PropertiesService

  trait PropertiesService {
    def getInstanceName(): String

    def changeInstanceName(newName: String)(implicit principal: Principal): AuthorizationResult[Unit]
  }

}

trait PropertiesServiceComponentImpl extends PropertiesServiceComponent {
  this: SessionProvider with PropertiesRepositoryComponent =>

  val propertiesService = new PropertiesServiceImpl

  class PropertiesServiceImpl extends PropertiesService {

    def getInstanceName(): String = withSession {
      implicit session =>
        val defaultValue = "ANTARCTICLE"
        val property = propertiesRepository.getProperty(PropertyNames.instanceNameProperty)

        property match {
          case Some(x) =>
            x.value.getOrElse(defaultValue)
          case None => defaultValue
        }
    }

    def changeInstanceName(newName: String)(implicit principal: Principal) = withTransaction {
      implicit session =>
        principal.doAuthorizedOrFail(Manage, Entities.Property) {
          () =>
            val property = propertiesRepository.getProperty(PropertyNames.instanceNameProperty)
            property match {
              case Some(x) =>
                propertiesRepository.updateProperty(PropertyNames.instanceNameProperty, Some(newName))
              case None =>
                propertiesRepository.createNewProperty(PropertyNames.instanceNameProperty, Some(newName))
            }
        }
    }
  }
}
