package services

import repositories.Repositories

trait Services extends ArticlesServiceComponentImpl {
  this: Repositories with SessionProvider =>
}