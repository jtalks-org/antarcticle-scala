package controllers

import play.api.mvc.{Action, Controller}
import security.Authentication
import security.Result.{Authorized, NotAuthorized}
import services.ApplicationPropertiesServiceComponent
import services.AppProperty._

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
          propertiesService.writeProperty(InstanceName, x) match {
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
            case _ if id.equals(TopBannerUrl.name) ||
              id.equals(BottomBannerUrl.name) =>
              propertiesService.writeProperty(id, value) match {
                case Authorized(created) => Ok("")
                case NotAuthorized() => Unauthorized("You are not authorized to perform this action")
              }
            case _ => BadRequest("Malformed or incomplete request")
          }
      }
  }
}
