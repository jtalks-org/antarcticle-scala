package util

import security.{AnonymousPrincipal, Principal, Authentication}
import repositories.UsersRepositoryComponent
import play.api.mvc.{RequestHeader, Controller}

/**
 *  Authentication mock for controller unit testing. By default returns
 *  AnonymousPrincipal, but may be customized
 */
trait FakeAuthentication extends Authentication
                          with Controller
                          with UsersRepositoryComponent
                          with FakeSessionProvider{

  var fakePrincipal : Principal = AnonymousPrincipal

  override implicit def currentPrincipal(implicit request: RequestHeader): Principal = fakePrincipal

  def setPrincipal(principal : Principal) = fakePrincipal = principal

  def reset() = fakePrincipal = AnonymousPrincipal
}
