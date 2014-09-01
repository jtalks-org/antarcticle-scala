package controllers

import play.api.mvc.{Controller, Action}
import services.{ApplicationPropertyNames, ApplicationPropertiesServiceComponent}
import security.Authentication
import security.Result.{NotAuthorized, Authorized}

/**
 * Manages application properties - global per-instance values, that
 * represent application configuration: application instance name,
 * default language and so on.
 */
trait ApplicationPropertiesController {
  this: Controller with ApplicationPropertiesServiceComponent with PropertiesProvider with Authentication =>

  def postChangedInstanceName() = Action(parse.json) {
    implicit request =>
      (request.body \ "instanceName").asOpt[String] match {
        case None => BadRequest("")
        case Some(x) =>
          propertiesService.writeProperty(ApplicationPropertyNames.instanceNameProperty, x) match {
            case Authorized(created) => Ok("")
            case NotAuthorized() => Unauthorized("You are not authorized to perform this action")
          }
      }
  }

  def postBannerId(id: String) = Action(parse.json) {
    implicit request =>
      (request.body \ "codepenId").asOpt[String] match {
        case None => BadRequest("")
        case Some(value) =>
          id match {
            case _ if id.equals(ApplicationPropertyNames.topBannerURL) ||
              id.equals(ApplicationPropertyNames.bottomBannerURL) =>
              propertiesService.writeProperty(id, value) match {
                case Authorized(created) => Ok("")
                case NotAuthorized() => Unauthorized("You are not authorized to perform this action")
              }
            case _ => BadRequest("Malformed or incomplete request")
          }
      }
  }
}
