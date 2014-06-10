package controllers

import models.ApplicationPropertyModels.MainTemplateProperties
import services.ApplicationPropertiesServiceComponent
import play.api.mvc.RequestHeader

trait PropertiesProvider {
  this: ApplicationPropertiesServiceComponent =>

  implicit def mainPageProperties(implicit request: RequestHeader): models.ApplicationPropertyModels.MainTemplateProperties = {
    val instanceName = propertiesService.getInstanceName()
    MainTemplateProperties(instanceName)
  }

}
