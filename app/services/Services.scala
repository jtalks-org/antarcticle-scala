package services

import repositories.Repositories
import validators.ArticleValidator
import validators.TagValidator

/**
 * Service layer implementation
 */
trait Services
  extends ArticlesServiceComponentImpl
  with TagsServiceComponentImpl {
  this: Repositories with SessionProvider =>

  override val articleValidator = new ArticleValidator
  override val tagValidator = new TagValidator
}
