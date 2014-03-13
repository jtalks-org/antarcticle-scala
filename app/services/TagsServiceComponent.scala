package services

import repositories.TagsRepositoryComponent
import scala.slick.jdbc.JdbcBackend
import validators.Validator
import scalaz._
import Scalaz._

trait TagsServiceComponent {
  val tagsService: TagsService

  trait TagsService {
    def createTagsForArticle(articleId: Int, tags: Seq[String])(implicit s: JdbcBackend#Session): ValidationNel[String, Seq[String]]

    def updateTagsForArticle(articleId: Int, tags: Seq[String])(implicit s: JdbcBackend#Session): ValidationNel[String, Seq[String]]
  }
}

trait TagsServiceComponentImpl extends TagsServiceComponent {
  this: SessionProvider with TagsRepositoryComponent =>

  val tagsService = new TagsServiceImpl
  val tagValidator: Validator[String]

  class TagsServiceImpl extends TagsService {

    override def createTagsForArticle(articleId: Int, tags: Seq[String])(implicit s: JdbcBackend#Session) = {
      val trimmedTags = tags.map(_.trim)
      validateTags(trimmedTags).map { _ =>
        val existingTags = tagsRepository.getByNames(trimmedTags)
        val existingTagsNames = existingTags.map(_.name)
        val newTags = trimmedTags.filterNot(existingTagsNames.contains)
        val newTagsIds = tagsRepository.insertTags(newTags)
        val allTagsId = newTagsIds ++ existingTags.map(_.id)
        val articleTags = allTagsId.map((articleId, _))
        tagsRepository.insertArticleTags(articleTags)
        trimmedTags
      }
    }

    override def updateTagsForArticle(articleId: Int, tags: Seq[String])(implicit s: JdbcBackend#Session) = {
      tagsRepository.removeArticleTags(articleId)
      createTagsForArticle(articleId, tags)
    }

    def validateTags(tags: Seq[String]) = {
      tags.map(tagValidator.validate).toList.sequence[({type λ[α]=ValidationNel[String, α]})#λ, String]
    }
  }
}
