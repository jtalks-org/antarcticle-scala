package security

import play.api.mvc.Controller
import repositories.UsersRepositoryComponent
import services.SessionProvider
import conf.PropertiesProviderComponent
import play.api.Logger
import conf.Keys
import scalaz._
import Scalaz._

trait SecurityComponent extends SecurityServiceComponentImpl with Authentication {
  this: Controller with UsersRepositoryComponent
    with SessionProvider with PropertiesProviderComponent=>

  override val authenticationManager = {

    def fake = {
      Logger.warn("Using fake authentication manager. DON'T USE IN PRODUCTION!!!")
      new FakeAuthenticationManager
    }

    def poulpeAndLocalDatabase = {
      val poulpeUrl = propertiesProvider.get[String](Keys.PoulpeUrl)
        .getOrElse(throw new RuntimeException("Poulpe URL not found. Check your configuration file."))
      Logger.warn(s"Using Poulpe authentication manager with Poulpe at $poulpeUrl")
      new CompositeAuthenticationManager(
        new PoulpeAuthenticationManager(poulpeUrl),
        new LocalDatabaseAuthenticationManager(this, this)
      )
    }

    def localDatabaseOnly = {
      Logger.warn("Using local database authentication manager")
      new LocalDatabaseAuthenticationManager(this, this)
    }

    propertiesProvider.get[Boolean](Keys.UseFakeAuthentication).cata(
      some = useFake => if (useFake) fake else poulpeAndLocalDatabase,
      none = poulpeAndLocalDatabase
    )
  }
}

