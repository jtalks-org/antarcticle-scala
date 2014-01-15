package services

import repositories.TagsRepositoryComponent
import scala.slick.jdbc.JdbcBackend
import validators.Validator
import scalaz._
import Scalaz._

trait TagsServiceComponent {
  val tagsService: TagsService

  trait TagsService {
    def createTagsForArticle(articleId: Int, tags: Seq[String]): ValidationNel[String, Seq[String]]
  }
}

trait TagsServiceComponentImpl extends TagsServiceComponent {
  this: SessionProvider with TagsRepositoryComponent =>

  val tagsService = new TagsServiceImpl
  val tagValidator: Validator[String]

  class TagsServiceImpl extends TagsService {
    def createTagsForArticle(articleId: Int, tags: Seq[String]) = withTransaction { implicit session =>
      val trimmedTags = tags.map(_.trim)

      validateTags(trimmedTags).map { _ =>
        val existingTags = tagsRepository.getByNames(trimmedTags)
        val existingTagsNames = existingTags.map(_.name)

        val newTags = trimmedTags.filterNot(existingTagsNames.contains)
        val newTagsIds = tagsRepository.insertAll(newTags)

        val allTagsId = newTagsIds ++ existingTags.map(_.id)

        val articleTags = allTagsId.map((articleId, _))

        tagsRepository.insertArticleTags(articleTags)
        trimmedTags
      }
    }

    def validateTags(tags: Seq[String]) = {
      tags.map(tagValidator.validate).toList.sequence[({type λ[α]=ValidationNel[String, α]})#λ, String]
    }
  }
}
