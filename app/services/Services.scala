package services

import repositories.Repositories

/**
 * Service layer implementation
 */
trait Services
  extends ArticlesServiceComponentImpl
  with TagsServiceComponentImpl {

  this: Repositories with SessionProvider =>
}