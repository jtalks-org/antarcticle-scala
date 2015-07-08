package services

import repositories.ApplicationPropertiesRepositoryComponent
import security.{Entities, Principal}
import security.Permissions.Manage
import security.Result.AuthorizationResult
import scala.language.implicitConversions

object AppProperty extends Enumeration {
  protected case class Val(name: String, defaultValue: Option[String]) extends super.Val
  type ApplicationProperty = Val

  val InstanceName = Val("INSTANCE_NAME", Some("ANTARCTICLE"))
  val TopBannerUrl = Val("topBanner", None)
  val BottomBannerUrl = Val("bottomBanner", None)

  implicit def propertyToString(prop: ApplicationProperty): String = prop.name
}

trait ApplicationPropertiesServiceComponent {
  val propertiesService: ApplicationPropertiesService

  trait ApplicationPropertiesService {
    def getInstanceName: String

    def getBannerUrl(id: String): Option[String]

    def writeProperty(id: String, value: String)(implicit principal: Principal): AuthorizationResult[Unit]
  }

}

trait ApplicationPropertiesServiceComponentImpl extends ApplicationPropertiesServiceComponent {
  this: SessionProvider with ApplicationPropertiesRepositoryComponent =>
  import AppProperty._

  val propertiesService = new ApplicationPropertiesServiceImpl

  class ApplicationPropertiesServiceImpl extends ApplicationPropertiesService {

    def getInstanceName: String = withSession {
      implicit session =>
        propertiesRepository.getProperty(InstanceName) match {
          case Some(x) => x.value.getOrElse(InstanceName.defaultValue.get)
          case None => InstanceName.defaultValue.get
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
