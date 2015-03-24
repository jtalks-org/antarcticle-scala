package controllers

import conf.{Keys, PropertiesProviderComponent}
import models.ApplicationPropertyModels.MainTemplateProperties
import services.{ApplicationPropertyNames, ApplicationPropertiesServiceComponent}
import play.api.mvc.RequestHeader

trait PropertiesProvider {
  this: ApplicationPropertiesServiceComponent with PropertiesProviderComponent =>

  implicit def mainPageProperties(implicit request: RequestHeader): models.ApplicationPropertyModels.MainTemplateProperties = {
    val instanceName = propertiesService.getInstanceName()
    val topBannerUrl = propertiesService.getBannerUrl(ApplicationPropertyNames.topBannerURL)
    val bottomBannerUrl = propertiesService.getBannerUrl(ApplicationPropertyNames.bottomBannerURL)
    val poulpeUrl: Option[String] = propertiesProvider.get[String](Keys.PoulpeUrl)
    val fakePoulpe: Boolean = propertiesProvider.get[Boolean](Keys.UseFakeAuthentication).getOrElse(false)
    MainTemplateProperties(instanceName,
      if(topBannerUrl == null) None else topBannerUrl,
      if(bottomBannerUrl == null) None else bottomBannerUrl,
      signUpAvailable = !fakePoulpe && (poulpeUrl.isEmpty || poulpeUrl.get.isEmpty)
    )
  }

}
