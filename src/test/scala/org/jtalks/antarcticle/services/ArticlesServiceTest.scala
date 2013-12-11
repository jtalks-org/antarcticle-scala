package org.jtalks.antarcticle.services

import org.scalatest._
import org.scalatest.mock.MockitoSugar
import org.jtalks.antarcticle.persistence.repositories.ArticlesRepositoryComponent
import org.mockito.Mockito._
import org.mockito.Matchers._
import org.jtalks.antarcticle.persistence.schema.{ArticleToUpdate, UserRecord, ArticleRecord}
import org.joda.time.DateTime
import org.jtalks.antarcticle.util.DateImplicits._
import org.jtalks.antarcticle.utils.TimeFridge
import scala.Some
import org.jtalks.antarcticle.services.Article
import org.jtalks.antarcticle.models.ArticleModels.{ArticleListModel, ArticleDetailsModel}


class ArticlesServiceTest extends FunSpec
                          with Matchers
                          with MockitoSugar
                          with OptionValues {

  trait MockArticlesRepositoryComponent extends ArticlesRepositoryComponent  {
    val repo = mock[ArticlesRepository]
    val articlesRepository = repo
  }

  val service = new ArticlesServiceComponentImpl with MockArticlesRepositoryComponent

  import service._

  describe("creating new article") {
    it("inserts new article") {
      TimeFridge.withFrozenTime() { dt =>
        val record = ArticleRecord(None, "", "", dt, dt, "", 1)
        when(articlesRepository.insert(any())).thenReturn(record.copy(id=Some(0)))

        articlesService.createArticle(Article(None, "", "", List()))

        verify(articlesRepository).insert(record)
      }
    }

    it("returns model") {
      val record = ArticleRecord(Some(1), "", "", DateTime.now, DateTime.now, "", 1)
      when(articlesRepository.insert(any())).thenReturn(record)

      val model: ArticleDetailsModel = articlesService.createArticle(Article(None, "", "", List()))

      model.id shouldBe (1)
    }
  }

  describe("paginated articles list") {
    it("requests second page with correct parameters") {
      when(articlesRepository.getList(any(), any())).thenReturn(List())

      articlesService.getPage(2)

      verify(articlesRepository).getList(6, 3)
    }

    it("returns list models") {
      val res = List((ArticleRecord(Some(1), "", "", DateTime.now, DateTime.now, "", 1), UserRecord(Some(1), "")))
      when(articlesRepository.getList(any(), any())).thenReturn(res)

      val model: List[ArticleListModel] = articlesService.getPage(1)

      model(0).id shouldBe (1)
      model(0).author.id shouldBe (1)
    }
  }

  describe("article removal") {
    it("removes article") {
      articlesService.removeArticle(1)

      verify(articlesRepository).remove(1)
    }
  }

  describe("article update") {
    it("updates existing article") {
      articlesService.updateArticle(Article(Some(1), "title", "content", List("tag")))

      verify(articlesRepository).update(1, ArticleToUpdate("title", "content", any(), any()))
    }

    it("updates modification time") {
      TimeFridge.withFrozenTime() { now =>
        articlesService.updateArticle(Article(Some(1), "", "", List("")))

        verify(articlesRepository).update(1, ArticleToUpdate("", "", now, "")) //TODO: match only modification time
      }
    }
  }

  describe("get article") {
    it("returns model") {
      val record = ArticleRecord(Some(1), "", "", DateTime.now, DateTime.now, "", 1)
      val user = UserRecord(Some(1), "")
      when(articlesRepository.get(any())).thenReturn(Some((record, user)))

      val model  = articlesService.get(1)

      model.value.id shouldBe (1)
      model.value.author.id shouldBe (1)
    }
  }
}
