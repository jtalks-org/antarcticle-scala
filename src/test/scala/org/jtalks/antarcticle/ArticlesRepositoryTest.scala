package org.jtalks.antarcticle

import org.jtalks.antarcticle.persistence.repositories.SlickArticlesRepositoryComponent
import org.jtalks.antarcticle.persistence._
import java.sql.Timestamp
import org.jtalks.antarcticle.persistence.schema.{ArticleToUpdate, UserRecord, ArticleRecord}
import org.scalatest.OptionValues
import com.github.nscala_time.time.Imports._
import org.joda.time.DateTime
import org.jtalks.antarcticle.util.DateImplicits._

//TODO: create something like "beforeDbTest" for fixtures
class ArticlesRepositoryTest extends RepositorySpec with OptionValues {

  val repository = new SlickArticlesRepositoryComponent
        with TestDbProvider
        with Schema

  override def schema = repository

  import repository._
  import profile.simple._

  def populateDb(implicit session: Session) = {
    val time = DateTime.now
    val users = List(UserRecord(None, "user1"),  UserRecord(None, "user2"))
    val articles = List(
      ArticleRecord(None, "New title 1", "<b>content</b>", time + 1.day, time, "description1", 1),
      ArticleRecord(None, "New title 2", "<i>html text</i>", time, time, "description2", 2),
      ArticleRecord(None, "New title 3", "<i>html text</i>", time + 2.days, time, "description3", 2),
      ArticleRecord(None, "New title 4", "<i>html text</i>", time + 4.days, time, "description4", 2)
    )

    Users.autoInc.insertAll(users : _*)
    val articlesIds = Articles.autoInc.insertAll(articles: _*)

    val articlesWithId = articles.zipWithIndex.map {
      case (article, idx) => article.copy(id = Some(articlesIds(idx)))
    }
    (users, articlesWithId)
  }

  describe("list portion") {
    it("returns portion with size 2") { implicit session: Session =>
      val (_, articles) = populateDb

      val offset = 0
      val portionSize = 2

      val portion = articlesRepository.getList(offset, portionSize)

      portion.size shouldBe (2)
    }

    it("returns portion with offset 1") { implicit session: Session =>
      val (_, articles) = populateDb
      val secondArticle = articles(1)

      val offset = 1
      val portionSize = 2

      val portion = articlesRepository.getList(offset, portionSize)

      val firstArticleInPortion = portion(0)._1
      firstArticleInPortion shouldBe (secondArticle)
    }

    it("sorted by creation time") { implicit session: Session =>
      val (_, articles) = populateDb

      val portion = articlesRepository.getList(0, 2)

      portion.map(_._1) should contain theSameElementsInOrderAs List(articles(1), articles(0))
    }
    //TODO: test author correctness
  }

  describe("article by id") {
    it("return article with id 2") { implicit session: Session =>
      val (_, articles) = populateDb

      val article = articlesRepository.get(2)

      val articleId = article.value._1.id.value
      articleId shouldBe (2)
    }

    it("returns None when there are no article with id 2000") { implicit session: Session =>
      val (_, articles) = populateDb

      val article = articlesRepository.get(2000)

      article shouldBe (None)
    }
  }

  describe("inserting new article") {
    it("inserts new article record") { implicit session: Session =>
      val (_, articles) = populateDb
      val oldCount = articles.size

      val userId = 2
      val newArticle = ArticleRecord(None, "test article", "content", DateTime.now, DateTime.now, "descr", userId)

      articlesRepository.insert(newArticle)

      articlesCount shouldBe(oldCount + 1)
    }

    it("assigns id to new article") { implicit session: Session =>
      val (_, articles) = populateDb

      val userId = 2
      val newArticle = ArticleRecord(None, "test article", "content", DateTime.now, DateTime.now, "descr", userId)

      val insertedArticle = articlesRepository.insert(newArticle)

      insertedArticle.id should not be (None)
    }
  }

  describe("updating article") {
    it("updates existing article") { implicit session: Session =>
      val (_, articles) = populateDb
      val articleToBeUpdated = articles(1)

      val newContent = "new content"
      val upd = ArticleToUpdate(articleToBeUpdated.title,
        newContent, articleToBeUpdated.createdAt, articleToBeUpdated.description)

      //TODO: split assertions
      articlesRepository.update(articleToBeUpdated.id.get, upd) shouldBe (true)
      val actualContent = Query(Articles).filter(_.id === articleToBeUpdated.id).map(_.content).first
      actualContent shouldBe (newContent)
    }

    it("returns false when updating not existing article") { implicit session: Session =>
      val upd = ArticleToUpdate("title", "content", DateTime.now, "desc")

      articlesRepository.update(2000, upd) shouldBe (false)
    }
  }

  describe("removing article") {
    it("removes article") { implicit session: Session =>
      val (_, articles) = populateDb
      val oldCount = articles.size

      articlesRepository.remove(2) shouldBe (true)
      articlesCount shouldBe (oldCount - 1)
    }

    it("removes expected article") { implicit session: Session =>
      val (_, articles) = populateDb
      val articleId = 2

      articlesRepository.remove(articleId)

      Query(Articles).filter(_.id === articleId).list.length shouldBe (0)
    }

    it("returns false when article not exists") { implicit session: Session =>
      articlesRepository.remove(1000) shouldBe (false)
    }
  }

  def articlesCount(implicit session: Session) = Query(Articles.length).first
}
