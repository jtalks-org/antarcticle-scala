package controllers

import play.api.mvc.{Action, Controller}

trait WebJarController {
  this: Controller =>

  def file(file: String) = Action(implicit request => Ok(""))

}

trait WebJarControllerImpl extends WebJarController{
  this: Controller =>

  override def file(file: String) = controllers.WebJarAssets.at(WebJarAssets.locate(file))

}
