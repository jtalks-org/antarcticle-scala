package services

import repositories.Repositories

/**
 * Service layer implementation
 */
trait Services extends ArticlesServiceComponentImpl {
  this: Repositories with SessionProvider =>
}