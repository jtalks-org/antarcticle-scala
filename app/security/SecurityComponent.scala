package security

import conf.{Keys, PropertiesProviderComponent}
import play.api.Logger
import play.api.mvc.Controller
import repositories.UsersRepositoryComponent
import services.SessionProvider

import scalaz.Scalaz._

trait SecurityComponent extends SecurityServiceComponentImpl with Authentication {
  this: Controller with UsersRepositoryComponent
    with SessionProvider with PropertiesProviderComponent =>

  override val authenticationManager = {

    def fake = {
      Logger.warn("Using fake authentication manager. DON'T USE IN PRODUCTION!!!")
      new FakeAuthenticationManager
    }

    def poulpeAndLocalDatabase = {
      val poulpeUrl = propertiesProvider.get[String](Keys.PoulpeUrl)
      poulpeUrl match {
        case Some(url) => Logger.warn(s"Using Poulpe authentication manager with Poulpe at $url")
        case None => Logger.warn("Using local database authentication manager")
      }
      new CompositeAuthenticationManager(
        poulpeUrl.map(url => new PoulpeAuthenticationManager(url)),
        new LocalDatabaseAuthenticationManager(this, this)
      )
    }

    propertiesProvider.get[Boolean](Keys.UseFakeAuthentication).cata(
      some = useFake => if (useFake) fake else poulpeAndLocalDatabase,
      none = poulpeAndLocalDatabase
    )
  }
}

