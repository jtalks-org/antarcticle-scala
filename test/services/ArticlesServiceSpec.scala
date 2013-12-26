package services

import org.specs2.mutable.Specification
import models.database._
import utils.DateImplicits._
import org.specs2.time.NoTimeConversions
import com.github.nscala_time.time.Imports._
import org.specs2.mock.Mockito
import repositories.ArticlesRepositoryComponent
import org.mockito.Matchers
import models.ArticleModels.{ArticleDetailsModel, Article, ArticleListModel}
import util.{FakeSessionProvider, TimeFridge}
import util.FakeSessionProvider.FakeSessionValue
import models.Page



class ArticlesServiceSpec extends Specification with NoTimeConversions with Mockito {
  object service extends ArticlesServiceComponentImpl
      with ArticlesRepositoryComponent with FakeSessionProvider {
    override val articlesRepository = mock[ArticlesRepository]
  }

  import service._

  val dbRecord = {
    val article = ArticleRecord(Some(1), "", "", DateTime.now, DateTime.now, "", 1)
    val user = UserRecord(Some(1), "")
    val tags = List("tag1", "tag2")
    (article, user, tags)
  }

  "get article" should {
    val article = Some(dbRecord)

    "return model" in {
      articlesRepository.get(anyInt)(Matchers.eq(FakeSessionValue)) returns article

      val model = articlesService.get(1)

      model.map(_.id) must beSome(1)
    }

    "have correct author" in {
      articlesRepository.get(anyInt)(Matchers.eq(FakeSessionValue)) returns article

      val model = articlesService.get(1)

      model.map(_.author.id) must beSome(1)
    }

    "have correct tags" in {
      articlesRepository.get(anyInt)(Matchers.eq(FakeSessionValue)) returns article

      val model = articlesService.get(1)

      model.map(_.tags).get must containTheSameElementsAs(dbRecord._3)
    }
  }

  "article removal" should {
    "remove article" in {
      articlesService.removeArticle(1)

      there was one(articlesRepository).remove(1)(FakeSessionValue)
    }
  }

  "paginated articles list" should {
    "request second page with correct parameters" in {
      articlesRepository.getList(anyInt, anyInt)(Matchers.eq(FakeSessionValue)) returns List()

      articlesService.getPage(2)

      there was one(articlesRepository).getList(6, 3)(FakeSessionValue)
    }

    "contain list models" in {
      val res = List(dbRecord)
      articlesRepository.getList(anyInt, anyInt)(Matchers.eq(FakeSessionValue)) returns res

      val model: Page[ArticleListModel] = articlesService.getPage(1)

      model.list(0).id must_== 1
      model.list(0).author.id must_== 1
    }

    "contain current page" in {
      val model = articlesService.getPage(1)

      model.currentPage must_== 1
    }

    "contain total pages count" in {
      val count = 5
      articlesRepository.count()(Matchers.eq(FakeSessionValue)) returns count

      val model = articlesService.getPage(1)

      model.totalPages must_== 2
    }
  }

  "creating new article" should {
    "insert new article" in {
      TimeFridge.withFrozenTime() { dt =>
        val record = ArticleToInsert("", "", dt, dt, "", 1)
        articlesRepository.insert(any[ArticleToInsert])(Matchers.eq(FakeSessionValue)) returns 1

        articlesService.createArticle(Article(None, "", "", List()))

        there was one(articlesRepository).insert(record)(FakeSessionValue)
      }
    }

    "return model" in {
      val record = ArticleToInsert("", "", DateTime.now, DateTime.now, "", 1)
      articlesRepository.insert(any[ArticleToInsert])(Matchers.eq(FakeSessionValue)) returns 1

      val model: ArticleDetailsModel = articlesService.createArticle(Article(None, "", "", List()))

      model.id must_== 1
    }
  }

  "article update" should {
    "update existing article" in {
      articlesService.updateArticle(Article(Some(1), "title", "content", List("tag")))

      there was one(articlesRepository).update(
        Matchers.eq(1), any[ArticleToUpdate])(Matchers.eq(FakeSessionValue))
    }

    "update modification time" in {
      TimeFridge.withFrozenTime() { now =>
        articlesService.updateArticle(Article(Some(1), "", "", List("")))

        there was one(articlesRepository).update(1, ArticleToUpdate("", "", now, ""))(FakeSessionValue) //TODO: match only modification time
      }
    }
  }
}
