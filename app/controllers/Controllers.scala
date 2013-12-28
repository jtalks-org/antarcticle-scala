package controllers

import play.api.mvc.Controller
import services.Services

/**
 * Controllers layer implementation
 */
trait Controllers extends Controller
                  with ArticleController
                  with AuthenticationController
                  with UserController{
  this: Services =>
}