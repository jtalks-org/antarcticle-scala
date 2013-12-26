package services

import org.specs2.mutable.Specification
import util.FakeSessionProvider
import repositories.TagsRepositoryComponent
import util.FakeSessionProvider.FakeSessionValue
import org.mockito.Matchers
import models.database.Tag
import org.specs2.specification.BeforeExample
import org.specs2.mock.Mockito
import org.specs2.mock.mockito.ArgumentCapture

class TagsServiceSpec extends Specification with Mockito with BeforeExample {
  object service extends TagsServiceComponentImpl
                  with TagsRepositoryComponent
                  with FakeSessionProvider {
    override val tagsRepository = mock[TagsRepository]
  }

  import service._

  def before = org.mockito.Mockito.reset(tagsRepository)

  "tags creation for article" should {
    val tags = List("tag", " tag1 ", "tag2 ")

    "trim tags with trailing spaces" in {
      tagsRepository.getByNames(any[Seq[String]])(Matchers.eq(FakeSessionValue)) returns List[Tag]()
      tagsRepository.insertAll(any[Seq[String]])(Matchers.eq(FakeSessionValue)) returns (1 to tags.size)

      tagsService.createTagsForArticle(1, tags)

      val trimmedTags = List("tag", "tag1", "tag2")
      there was one(tagsRepository).getByNames(trimmedTags)(FakeSessionValue)
    }

    "create only new tags" in {
      val existingTags = List(Tag(1, "tag"), Tag(2, "tag2"))
      val newTag = "tag1"
      val newTagId = 5
      tagsRepository.getByNames(any[Seq[String]])(Matchers.eq(FakeSessionValue)) returns existingTags
      tagsRepository.insertAll(any[Seq[String]])(Matchers.eq(FakeSessionValue)) returns List(newTagId)

      tagsService.createTagsForArticle(1, tags)

      there was one(tagsRepository).insertAll(List(newTag))(FakeSessionValue)
    }

    "create article association with new tags" in {
      val tagsIds = 1 to tags.size
      tagsRepository.getByNames(any[Seq[String]])(Matchers.eq(FakeSessionValue)) returns List[Tag]()
      tagsRepository.insertAll(any[Seq[String]])(Matchers.eq(FakeSessionValue)) returns tagsIds

      tagsService.createTagsForArticle(1, tags)

      val assocListCapture = new ArgumentCapture[Seq[(Int, Int)]]()
      val expectedArticleTagsAssociations = Seq((1, 1), (1, 2), (1, 3))
      there was one(tagsRepository).insertArticleTags(assocListCapture.capture)(Matchers.eq(FakeSessionValue))
      assocListCapture.value must containTheSameElementsAs(expectedArticleTagsAssociations)
    }

    "creates article association with tags, when some tags exists" in {
      val existingTag = Tag(1, "tag1")
      val newTagsIds = List(2, 3)
      tagsRepository.getByNames(any[Seq[String]])(Matchers.eq(FakeSessionValue)) returns List(existingTag)
      tagsRepository.insertAll(any[Seq[String]])(Matchers.eq(FakeSessionValue)) returns newTagsIds

      tagsService.createTagsForArticle(1, tags)

      val assocListCapture = new ArgumentCapture[Seq[(Int, Int)]]()
      val expectedArticleTagsAssociations = Seq((1, 1), (1, 2), (1, 3))
      there was one(tagsRepository).insertArticleTags(assocListCapture.capture)(Matchers.eq(FakeSessionValue))
      assocListCapture.value must containTheSameElementsAs(expectedArticleTagsAssociations)
    }
  }
}
