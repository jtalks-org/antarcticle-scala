package controllers

import play.api.mvc.Controller
import security.Authentication
import play.api.mvc.Action

/**
 * Renders application help pages
 */
trait HelpController {
  this: Controller with PropertiesProvider with Authentication =>

  def markdownHelp() = Action {
    implicit request => Ok(views.html.help.markdown())
  }

  def adminHelp() = Action {
    implicit request => Ok(views.html.help.admin())
  }
}
