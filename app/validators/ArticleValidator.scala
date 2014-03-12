package validators

import scalaz._
import Scalaz._
import models.ArticleModels.Article

object ArticleValidator {
  val MAX_CONTENT_LENGTH = 65000
  val MAX_TITLE_LENGTH = 60
  val MAX_TAGS_COUNT = 10
}

class ArticleValidator(tagValidator: Validator[String]) extends Validator[Article] {
  import ArticleValidator._

  def validate(article: Article): ValidationNel[String, Article] = {
    def checkContentLength = {
      if (article.content.length > MAX_CONTENT_LENGTH)
        s"Article text should not exceed $MAX_CONTENT_LENGTH characters".failNel else article.successNel
    }
    def checkTitleLength = {
      if (article.title.trim.isEmpty) "Title should not be blank".failNel
      else if (article.title.length > MAX_TITLE_LENGTH)
        s"Title should not exceed $MAX_TITLE_LENGTH characters".failNel else article.successNel
    }
    def checkTagsCount = if (article.tags.distinct.length > MAX_TAGS_COUNT)
      s"At most $MAX_TAGS_COUNT different tags are allowed".failNel else article.successNel

    def checkTags = article.tags.map(tagValidator.validate).toList.sequence[({type λ[α]=ValidationNel[String, α]})#λ, String]

    (checkTitleLength |@| checkContentLength |@| checkTagsCount |@|checkTags) {
      case _ => article
    }
  }
}
