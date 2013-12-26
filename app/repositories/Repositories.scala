package repositories

import models.database.{Profile, Schema}

/**
 * Repositories implementation
 */
trait Repositories extends SlickArticlesRepositoryComponent {
  this: Schema with Profile =>
}