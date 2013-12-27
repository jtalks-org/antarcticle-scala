package controllers

import play.api.mvc.Controller
import services.Services

/**
 * Controllers layer implementation
 */
trait Controllers extends Controller
                  with ArticlesController
                  with AuthenticationController
                  {
  this: Services =>
}