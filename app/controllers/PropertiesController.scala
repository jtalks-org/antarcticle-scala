package controllers

import play.api.mvc.{Controller, Action}
import services.PropertiesServiceComponent
import security.Authentication


trait PropertiesController {
  this: Controller with PropertiesServiceComponent with Authentication =>

  def postChangedInstanceName() = Action(parse.json) {
    implicit request =>
      val instanceName = (request.body \ "instanceName").asOpt[String]
      instanceName match {
        case None => BadRequest("")
        case Some(x) =>
          propertiesService.changeInstanceName(x)
          Ok("")
      }
  }
}
