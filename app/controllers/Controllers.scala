package controllers

import play.api.mvc.Controller
import services.Services

trait Controllers extends Controller with ArticlesController {
  this: Services =>
}