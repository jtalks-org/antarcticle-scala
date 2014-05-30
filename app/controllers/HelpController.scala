package controllers

import play.api.mvc.Controller
import security.Authentication
import play.api.mvc.Action
import services.PropertiesServiceComponent

/**
 * Renders application help pages
 */
trait HelpController {
  this: Controller with PropertiesServiceComponent with Authentication =>

  def markdownHelp() = Action {
    implicit request => Ok(views.html.help.markdown(propertiesService.getInstanceName()))
  }
}
