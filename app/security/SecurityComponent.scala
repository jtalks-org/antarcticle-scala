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

  override val tokenProvider = new UUIDTokenProvider

  override val authenticationManager = {
    def fake = {
      Logger.warn("Using fake authentication manager. DON'T USE IN PRODUCTION!!!")
      new FakeAuthenticationManager
    }
    def poulpe = {
      val poulpeUrl = propertiesProvider.get[String](Keys.PoulpeUrl)
        .getOrElse(throw new RuntimeException("Poulpe url not found. Check your configuration file."))
      Logger.warn(s"Using poulpe authentication manager with Poulpe at $poulpeUrl")
      new PoulpeAuthenticationManager(poulpeUrl)
    }

    propertiesProvider.get[Boolean](Keys.UseFakeAuthentication).cata(
      some = useFake => if (useFake) fake else poulpe,
      none = poulpe
    )
  }
}

