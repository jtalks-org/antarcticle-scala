package controllers

import models.ApplicationPropertyModels.MainTemplateProperties
import services.{ApplicationPropertyNames, ApplicationPropertiesServiceComponent}
import play.api.mvc.RequestHeader

trait PropertiesProvider {
  this: ApplicationPropertiesServiceComponent =>

  implicit def mainPageProperties(implicit request: RequestHeader): models.ApplicationPropertyModels.MainTemplateProperties = {
    val instanceName = propertiesService.getInstanceName()
    val topBannerUrl = propertiesService.getBannerUrl(ApplicationPropertyNames.topBannerURL)
    val bottomBannerUrl = propertiesService.getBannerUrl(ApplicationPropertyNames.bottomBannerURL)
    MainTemplateProperties(instanceName,
      if(topBannerUrl == null) None else topBannerUrl,
      if(bottomBannerUrl == null) None else bottomBannerUrl
    )
  }

}
