package security

import play.api.mvc.Controller
import repositories.UsersRepositoryComponent
import services.SessionProvider
import conf.PropertiesProviderComponent
import play.api.Logger
import scalaz._
import Scalaz._

trait SecurityComponent extends SecurityServiceComponentImpl with Authentication {
  this: Controller with UsersRepositoryComponent
    with SessionProvider with PropertiesProviderComponent=>

  override val tokenProvider = new UUIDTokenProvider

  override val authenticationManager =
    propertiesProvider.get[String]("ANTARCTICLE_POULPE_URL").cata(
      some = poulpeUrl => new PoulpeAuthenticationManager(poulpeUrl),
      none = {
        Logger.warn("Using fake authentication manager. DON'T USE IN PRODUCTION!!!")
        new FakeAuthenticationManager
      }
    )
}

