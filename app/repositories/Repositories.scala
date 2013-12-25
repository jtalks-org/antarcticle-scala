package repositories

import models.database.{Profile, Schema}

trait Repositories extends SlickArticlesRepositoryComponent {
  this: Schema with Profile =>
}