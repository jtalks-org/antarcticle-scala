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
          val changeResult = propertiesService.changeInstanceName(x)
          changeResult.fold(
            fail = nel => Forbidden(""),
            succ = nel => Ok("")
          )
      }
  }
}
