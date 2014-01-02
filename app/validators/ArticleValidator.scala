package validators

import scalaz._
import Scalaz._
import models.ArticleModels.Article

object ArticleValidator {
  val MAX_CONTENT_LENGTH = 65000
  val MAX_TITLE_LENGTH = 60
  val MAX_TAGS_COUNT = 10
}

class ArticleValidator extends Validator[Article] {
  import ArticleValidator._

  def validate(article: Article): ValidationNel[String, Article] = {
    def checkContentLength = {
      if (article.content.length > MAX_CONTENT_LENGTH) "Content is too long".failNel else article.successNel
    }
    def checkTitleLength = {
      if (article.title.trim.isEmpty) "Title should not be blank".failNel
      else if (article.title.length > MAX_TITLE_LENGTH) "Title is too long".failNel else article.successNel
    }
    def chackTagsCount = if (article.tags.length > MAX_TAGS_COUNT) "Too many tags".failNel else article.successNel

    (checkTitleLength |@| checkContentLength |@| chackTagsCount) {
      case _ => article
    }
  }
}
