package util

import security.{AnonymousPrincipal, Principal, Authentication}
import repositories.UsersRepositoryComponent
import play.api.mvc.{RequestHeader, Controller}

/**
 *  Authentication mock for controller unit testing
 */
trait FakeAuthentication extends Authentication
                          with Controller
                          with UsersRepositoryComponent
                          with FakeSessionProvider{

  override implicit def currentPrincipal(implicit request: RequestHeader): Principal = AnonymousPrincipal
}
