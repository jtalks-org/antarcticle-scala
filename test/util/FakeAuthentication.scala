package util

import security.Authentication
import repositories.UsersRepositoryComponent
import play.api.mvc.Controller

/**
 *  Authentication mock for controller unit testing
 */
trait FakeAuthentication extends Authentication
                          with Controller
                          with UsersRepositoryComponent
                          with FakeSessionProvider{
}
