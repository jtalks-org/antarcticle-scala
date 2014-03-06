import play.api.Logger
import scala.concurrent.ExecutionContext
import ExecutionContext.Implicits.global
import play.api.mvc.Results._
import play.api.mvc.RequestHeader
import scala.concurrent.Future
import security.AnonymousPrincipal

object Global extends play.api.GlobalSettings {

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
    }
  }

  /**
   * Setup global 404 (Not Found) page
   */
  override def onHandlerNotFound(request: RequestHeader): Future[play.api.mvc.SimpleResult] = {
    Future.successful(NotFound(views.html.errors.notFound()(AnonymousPrincipal)))
  }
}