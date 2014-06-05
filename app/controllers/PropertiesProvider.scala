package controllers

import models.PropertyModels.MainPageProperties
import services.PropertiesServiceComponent
import play.api.mvc.RequestHeader

trait PropertiesProvider {
  this: PropertiesServiceComponent =>

  implicit def mainPageProperties(implicit request: RequestHeader): models.PropertyModels.MainPageProperties = {
    val instanceName = propertiesService.getInstanceName()
    MainPageProperties(instanceName)
  }

}
