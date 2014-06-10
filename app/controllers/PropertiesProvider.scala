package controllers

import models.PropertyModels.MainPageProperties
import services.ApplicationPropertiesServiceComponent
import play.api.mvc.RequestHeader

trait PropertiesProvider {
  this: ApplicationPropertiesServiceComponent =>

  implicit def mainPageProperties(implicit request: RequestHeader): models.PropertyModels.MainPageProperties = {
    val instanceName = propertiesService.getInstanceName()
    MainPageProperties(instanceName)
  }

}
