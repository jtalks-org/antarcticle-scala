import play.api.Logger

object Global extends play.api.GlobalSettings {
  /*
   * Get controller instances as Application instance, because all controllers
   * mixed in into it.
   */
  override def getControllerInstance[A](controllerClass: Class[A]): A = {
    try {
      controllers.Application.asInstanceOf[A]
    } catch {
      case e: ClassCastException =>
        Logger.error(s"Controller ${controllerClass.getName} not mixed in into Application object")
        throw e
    }
  }
}