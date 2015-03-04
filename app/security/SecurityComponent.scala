package security

import conf.{Keys, PropertiesProviderComponent}
import play.api.Logger
import play.api.mvc.Controller
import repositories.UsersRepositoryComponent
import services.{MailServiceComponent, ApplicationPropertiesServiceComponent, SessionProvider}

trait SecurityComponent extends SecurityServiceComponentImpl with Authentication {
  this: Controller with UsersRepositoryComponent with ApplicationPropertiesServiceComponent
    with SessionProvider with PropertiesProviderComponent with MailServiceComponent =>

  override val authenticationManager = {

    def fakePoulpe = {
      Logger.warn("Using fake authentication manager. DON'T USE IN PRODUCTION!!!")
      new FakeAuthenticationManager
    }

    def poulpe = {
      propertiesProvider.get[String](Keys.PoulpeUrl) match {
        case Some(url) if !url.isEmpty =>
          Logger.warn(s"Using Poulpe authentication manager with Poulpe at $url")
          Some(new PoulpeAuthenticationManager(url))
        case _ =>
          Logger.warn("Using local database authentication manager")
          None
      }
    }

    def poulpeAuthManager = propertiesProvider.get[Boolean](Keys.UseFakeAuthentication) match {
      case Some(useFake) if useFake => Some(fakePoulpe)
      case _ => poulpe
    }

    new CompositeAuthenticationManager(poulpeAuthManager, new LocalDatabaseAuthenticationManager(this, this))
  }
}

