package controllers

import models.ApplicationPropertyModels.MainTemplateProperties
import services.ApplicationPropertiesServiceComponent
import play.api.mvc.RequestHeader

trait PropertiesProvider {
  this: ApplicationPropertiesServiceComponent =>

  // todo: temporary storage until persistence is available
  var topBannerId : Option[String] = None
  var bottomBannerId : Option[String] = None

  implicit def mainPageProperties(implicit request: RequestHeader): models.ApplicationPropertyModels.MainTemplateProperties = {
    val instanceName = propertiesService.getInstanceName()
    MainTemplateProperties(instanceName, topBannerId, bottomBannerId)
  }

}
