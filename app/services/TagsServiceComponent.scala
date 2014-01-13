package services

import repositories.TagsRepositoryComponent
import scala.slick.jdbc.JdbcBackend.Session
import validators.Validator
import scalaz._
import Scalaz._

trait TagsServiceComponent {
  val tagsService: TagsService

  trait TagsService {
    def createTagsForArticle(articleId: Int, tags: Seq[String]): ValidationNel[String, Unit]
  }
}

trait TagsServiceComponentImpl extends TagsServiceComponent {
  this: SessionProvider with TagsRepositoryComponent =>

  val tagsService = new TagsServiceImpl
  val tagValidator: Validator[String]

  class TagsServiceImpl extends TagsService {
    def createTagsForArticle(articleId: Int, tags: Seq[String]) = withTransaction { implicit s: Session =>
      val trimmedTags = tags.map(_.trim)

      validateTags(trimmedTags).map { _ =>
        val existingTags = tagsRepository.getByNames(trimmedTags)
        val existingTagsNames = existingTags.map(_.name)

        val newTags = trimmedTags.filterNot(existingTagsNames.contains)
        val newTagsIds = tagsRepository.insertAll(newTags)

        val allTagsId = newTagsIds ++ existingTags.map(_.id)

        val articleTags = allTagsId.map((articleId, _))

        tagsRepository.insertArticleTags(articleTags)
      }
    }

    def validateTags(tags: Seq[String]) = {
      tags.map(tagValidator.validate).toList.sequence[({type λ[α]=ValidationNel[String, α]})#λ, String]
    }
  }
}
