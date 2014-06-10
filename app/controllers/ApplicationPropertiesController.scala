package controllers

import play.api.mvc.{Controller, Action}
import services.ApplicationPropertiesServiceComponent
import security.Authentication
import security.Result.{NotAuthorized, Authorized}


trait ApplicationPropertiesController {
  this: Controller with ApplicationPropertiesServiceComponent with Authentication =>

  def postChangedInstanceName() = Action(parse.json) {
    implicit request =>
      (request.body \ "instanceName").asOpt[String] match {
        case None => BadRequest("")
        case Some(x) =>
          propertiesService.changeInstanceName(x) match {
            case Authorized(created) => Ok("")
            case NotAuthorized() => Unauthorized("You are not authorized to create comments")
          }
      }
  }
}
