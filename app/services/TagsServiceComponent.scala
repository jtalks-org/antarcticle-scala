package services

import repositories.TagsRepositoryComponent
import scala.slick.session.Session

trait TagsServiceComponent {
  val tagsService: TagsService

  trait TagsService {
    def createTagsForArticle(articleId: Int, tags: Seq[String]): Unit
  }
}

trait TagsServiceComponentImpl extends TagsServiceComponent {
  this: SessionProvider with TagsRepositoryComponent =>

  val tagsService = new TagsServiceImpl

  class TagsServiceImpl extends TagsService {
    def createTagsForArticle(articleId: Int, tags: Seq[String]) = withTransaction { implicit s: Session =>
      val trimmedTags = tags.map(_.trim)

      val existingTags = tagsRepository.getByNames(trimmedTags)
      val existingTagsNames = existingTags.map(_.name)

      val newTags = trimmedTags.filterNot(existingTagsNames.contains)
      val newTagsIds = tagsRepository.insertAll(newTags)

      val allTagsId = newTagsIds ++ existingTags.map(_.id)

      val articleTags = allTagsId.map((articleId, _))

      tagsRepository.insertArticleTags(articleTags)
    }
  }
}
