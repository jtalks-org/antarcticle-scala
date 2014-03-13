package controllers

import play.api.mvc.Controller
import security.Authentication
import play.api.mvc.Action

/**
 *
 */
trait HelpController {
  this: Controller with Authentication =>

  def markdownHelp() = Action {
    implicit request => Ok(views.html.help.markdown())
  }
}
