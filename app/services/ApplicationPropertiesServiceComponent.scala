package services

import repositories.ApplicationPropertiesRepositoryComponent
import security.{Entities, Principal}
import security.Permissions.Manage
import security.Result.AuthorizationResult


object ApplicationPropertyNames {
  val instanceNameProperty = "INSTANCE_NAME"
  val topBannerURL = "topBanner"
  val bottomBannerURL = "bottomBanner"
}

trait ApplicationPropertiesServiceComponent {
  val propertiesService: ApplicationPropertiesService

  trait ApplicationPropertiesService {
    def getInstanceName(): String

    def getBannerUrl(id: String): Option[String]

    def writeProperty(id: String, value: String)(implicit principal: Principal): AuthorizationResult[Unit]
  }

}

trait ApplicationPropertiesServiceComponentImpl extends ApplicationPropertiesServiceComponent {
  this: SessionProvider with ApplicationPropertiesRepositoryComponent =>

  val propertiesService = new ApplicationPropertiesServiceImpl

  class ApplicationPropertiesServiceImpl extends ApplicationPropertiesService {

    def getInstanceName: String = withSession {
      implicit session =>
        // todo: bind default into property class itself
        val defaultValue = "ANTARCTICLE"
        propertiesRepository.getProperty(ApplicationPropertyNames.instanceNameProperty) match {
          case Some(x) => x.value.getOrElse(defaultValue)
          case None => defaultValue
        }
    }

    def getBannerUrl(id: String) = withTransaction {
      implicit session =>
        propertiesRepository.getProperty(id) match {
          case Some(x) => x.value
          case None => None
        }
    }

    def writeProperty(id: String, value: String)(implicit principal: Principal) = withTransaction {
      implicit session =>
        principal.doAuthorizedOrFail(Manage, Entities.Property) { () =>
          propertiesRepository.getProperty(id) match {
            case Some(x) =>
              propertiesRepository.updateProperty(id, Some(value))
            case None =>
              propertiesRepository.createNewProperty(id, Some(value))
          }
        }
    }

  }

}
