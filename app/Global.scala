import controllers.IndexController
import controllers.filters.{CsrfFilter, KeepReferrerFilter}
import play.api
import play.api.mvc.Results._
import play.api.mvc.{Action, Handler, RequestHeader, SimpleResult, WithFilters}
import play.api.{GlobalSettings, Logger}
import play.libs.Akka

import scala.concurrent.Future

object Global extends WithFilters(CsrfFilter, KeepReferrerFilter) with GlobalSettings  {

  lazy val initialized = !getControllerInstance(classOf[IndexController]).isInstanceOf[FailedApplication.type]

  /*
   * Get controller instances as Application instance, because all controllers
   * mixed in into it.
   */
  override def getControllerInstance[A](controllerClass: Class[A]): A = {
    try {
      Application.asInstanceOf[A]
    } catch {
      case e: ClassCastException =>
        Logger.error(s"Controller ${controllerClass.getName} not mixed in into Application object")
        throw e
      case e: Error =>
          Logger.error("Could not initialize application" +  (Option(e.getMessage) match {
            case Some(msg) => s": $msg"
            case None => ""
          }))
        FailedApplication.asInstanceOf[A]
    }
  }

  private def isPublicAsset(path: String): Boolean =
    path == "/" || List("/stylesheets", "/javascripts", "/images", "/fonts").exists(p => path.startsWith(p))


  override def onRouteRequest(req: RequestHeader): Option[Handler] = {
    if (initialized || (req.method == "GET" && isPublicAsset(req.path))) {
       super.onRouteRequest(req)
    } else {
      Some(Action(Ok(views.html.errors.internalError())))
    }
  }

  /**
   * Decorates 400 (bad request) error with nice html, just in case user will see it
   */
  override def onBadRequest(request: RequestHeader, error: String): Future[SimpleResult] = {
    request.headers.get("X-Requested-With") match {
      case Some("XMLHttpRequest") =>
        // ajax request, don't bother decorating
        super.onBadRequest(request, error)
      case _ =>
        // this is likely to be submitted by user
        Logger.error(s"No suitable handler found for URL: ${request.uri}")
        // intentional combination: code shows the real error, while html displays more convenient explanation for the user
        Future.successful(BadRequest(views.html.errors.notFound()))
    }
  }

  /**
   * Setup global 500 page for any unhandled application exception
   */
  override def onError(request: RequestHeader, ex: Throwable): Future[SimpleResult] = {
    try {
      // check request origin, ajax requests don't need a full error page, just a message
      Future.successful(request.headers.get("X-Requested-With") match {
        case Some("XMLHttpRequest") =>
          InternalServerError(views.html.templates.formErrors(List(s"Error: ${ex.getMessage}")))
        case _ => Ok(views.html.errors.internalError())
      }
      )
    } catch {
      // the last thing we can do if even error page rendering fails
      case e: Throwable => super.onError(request, ex)
    }
  }

  /**
   * Setup global 404 (Not Found) page
   */
  override def onHandlerNotFound(request: RequestHeader): Future[SimpleResult] = {
    Logger.error(s"No suitable handler found for URL: ${request.uri}")
    Future.successful(NotFound(views.html.errors.notFound()))
  }


  override def onStart(app : api.Application) = {
    Logger.debug("Application has started")
  }

  override def onStop(app : api.Application) = {
    Logger.info("Shitting down akka system")
    Akka.system.shutdown()
  }


}