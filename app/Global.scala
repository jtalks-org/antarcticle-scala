object Global extends play.api.GlobalSettings {
  override def getControllerInstance[A](controllerClass: Class[A]): A = {
   controllers.Application.asInstanceOf[A]
  }
}