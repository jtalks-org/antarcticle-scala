package services

import repositories.PropertiesRepositoryComponent
import security.{AuthenticatedUser, Principal}
import scalaz._
import Scalaz._
import security.Result.AuthorizationResult

object PropertyNames {
  val instanceNameProperty = "INSTANCE_NAME"
}

trait PropertiesServiceComponent {
  val propertiesService: PropertiesService

  trait PropertiesService {
    def getInstanceName(): String

    def changeInstanceName(newName: String)(implicit principal: Principal): ValidationNel[String, String]
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

    def changeInstanceName(newName: String)(implicit principal: Principal): ValidationNel[String, String] = withSession {
      implicit session =>
        principal match {
          case user: AuthenticatedUser  => {
            propertiesRepository.changeProperty(PropertyNames.instanceNameProperty, Some(newName))
            ""
          }.successNel
          case _ => "Authentication required".failureNel
        }

    }
  }
}
