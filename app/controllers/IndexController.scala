package controllers

import play.api.mvc.{Action, Controller}

trait IndexController {
  this: Controller =>

  def index = Action {
    InternalServerError(views.html.errors.internalError())
  }
}
