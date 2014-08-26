package services

import repositories.ApplicationPropertiesRepositoryComponent
import security.{Entities, Principal}
import security.Permissions.Manage
import security.Result.AuthorizationResult


object ApplicationPropertyNames {
  val instanceNameProperty = "INSTANCE_NAME"
}

trait ApplicationPropertiesServiceComponent {
  val propertiesService: ApplicationPropertiesService

  trait ApplicationPropertiesService {
    def getInstanceName(): String

    def changeInstanceName(newName: String)(implicit principal: Principal): AuthorizationResult[Unit]

    def changeBannerId(id: String, value: String)(implicit principal: Principal): AuthorizationResult[Unit]
  }

}

trait ApplicationPropertiesServiceComponentImpl extends ApplicationPropertiesServiceComponent {
  this: SessionProvider with ApplicationPropertiesRepositoryComponent =>

  val propertiesService = new ApplicationPropertiesServiceImpl

  class ApplicationPropertiesServiceImpl extends ApplicationPropertiesService {

    def getInstanceName(): String = withSession {
      implicit session =>
        // todo: bind default into property class itself
        val defaultValue = "ANTARCTICLE"
        propertiesRepository.getProperty(ApplicationPropertyNames.instanceNameProperty) match {
          case Some(x) => x.value.getOrElse(defaultValue)
          case None => defaultValue
        }
    }

    def changeInstanceName(newName: String)(implicit principal: Principal) = withTransaction {
      implicit session =>
        principal.doAuthorizedOrFail(Manage, Entities.Property) { () =>
          propertiesRepository.getProperty(ApplicationPropertyNames.instanceNameProperty) match {
            case Some(x) =>
              propertiesRepository.updateProperty(ApplicationPropertyNames.instanceNameProperty, Some(newName))
            case None =>
              propertiesRepository.createNewProperty(ApplicationPropertyNames.instanceNameProperty, Some(newName))
          }
        }
    }

    def changeBannerId(id: String, value: String)(implicit principal: Principal) = {
      principal.doAuthorizedOrFail(Manage, Entities.Property) { () =>
        // todo
      }
    }

  }

}
