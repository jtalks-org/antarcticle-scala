package repositories

import models.database.{Profile, Schema}

/**
 * Repositories implementation
 */
trait Repositories
  extends SlickArticlesRepositoryComponent
  with TagsRepositoryComponentImpl {

  this: Schema with Profile =>
}