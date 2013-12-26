package repositories

import org.specs2.mutable._

import models.database._
import utils.DateImplicits._
import org.specs2.time.NoTimeConversions
import com.github.nscala_time.time.Imports._
import util.TestDatabaseConfiguration

class ArticlesRepositorySpec extends Specification with NoTimeConversions {
  object repository extends SlickArticlesRepositoryComponent with Schema with TestDatabaseConfiguration


  import repository._
  import profile.simple._

  def populateDb(implicit session: Session) = {
    createSchema

    val time = DateTime.now
    val users = List(UserToInsert("user1"), UserToInsert("user2"))
    val articles = List(
      ArticleToInsert("New title 1", "<b>content</b>", time + 1.day, time, "description1", 1),
      ArticleToInsert("New title 2", "<i>html text</i>", time, time, "description2", 2),
      ArticleToInsert("New title 3", "<i>html text</i>", time + 2.days, time, "description3", 2),
      ArticleToInsert("New title 4", "<i>html text</i>", time + 4.days, time, "description4", 2)
    )
    val tags = List("tag1", "tag2", "tag3")
    val articlesTags = List((1,1), (1,2), (2,1), (2,3))

    Tags.forInsert.insertAll(tags : _*)
    Users.forInsert.insertAll(users : _*)
    val articlesIds = Articles.forInsert.insertAll(articles: _*)
    ArticlesTags.insertAll(articlesTags : _*)
    (users, articles, articlesIds)
  }

  "list portion" should {
    "return portion with size 2" in withSession { implicit session: Session =>
      populateDb

      val offset = 0
      val portionSize = 2

      val portion = articlesRepository.getList(offset, portionSize)

      portion must have size 2
    }

    "return portion with offset 1" in withSession { implicit session: Session =>
      val (_, _, articlesIds) = populateDb
      val secondArticleId = articlesIds(1)

      val offset = 1
      val portionSize = 2

      val portion = articlesRepository.getList(offset, portionSize)

      val firstArticleInPortion = portion(0)._1
      firstArticleInPortion.id must beSome(secondArticleId)
    }

    "be sorted by creation time" in withSession { implicit session: Session =>
      val (_, _, articlesIds) = populateDb

      val portion = articlesRepository.getList(0, 2)

      portion.map(_._1).apply(0).id must beSome(articlesIds(1))
      portion.map(_._1).apply(1).id must beSome(articlesIds(0))
    }
    //TODO: test author correctness
  }


  "article by id" should {
    "return article with id 2" in withSession { implicit session: Session =>
      populateDb

      val article = articlesRepository.get(2)

      article must beSome
      article.flatMap(_._1.id) must beSome(2)
    }

    "return None when there are no article with id 2000" in withSession { implicit session: Session =>
      populateDb

      val article = articlesRepository.get(2000)

      article must beNone
    }
  }

  "inserting new article" should {
    "inserts new article record" in withSession { implicit session: Session =>
      val (_, articles, _) = populateDb
      val oldCount = articles.size

      val userId = 2
      val newArticle = ArticleToInsert("test article", "content", DateTime.now, DateTime.now, "descr", userId)

      articlesRepository.insert(newArticle)

      articlesCount must_== (oldCount + 1)
    }

    "assigns id to new article" in withSession { implicit session: Session =>
      populateDb

      val userId = 2
      val newArticle = ArticleToInsert("test article", "content", DateTime.now, DateTime.now, "descr", userId)

      val insertedArticleId = articlesRepository.insert(newArticle)
      true
    }
  }

  "updating article" should {
    "updates existing article" in withSession { implicit session: Session =>
      val (_, articles, articlesIds) = populateDb
      val articleToBeUpdated = articles(1)
      val updatedArticleId = articlesIds(1)

      val newContent = "new content"
      val upd = ArticleToUpdate(articleToBeUpdated.title,
        newContent, articleToBeUpdated.createdAt, articleToBeUpdated.description)

      //TODO: split assertions
      articlesRepository.update(updatedArticleId, upd) must beTrue
      val actualContent = Query(Articles).filter(_.id === updatedArticleId).map(_.content).first
      actualContent must_== newContent
    }

    "returns false when updating not existing article" in withSession { implicit session: Session =>
      populateDb

      val upd = ArticleToUpdate("title", "content", DateTime.now, "desc")

      articlesRepository.update(2000, upd) must beFalse
    }
  }

  "removing article" should {
    "removes article" in withSession { implicit session: Session =>
      val (_, articles, _) = populateDb
      val oldCount = articles.size

      articlesRepository.remove(2) must beTrue
      articlesCount must_== (oldCount - 1)
    }

    "removes expected article" in withSession { implicit session: Session =>
      populateDb
      val articleId = 2

      articlesRepository.remove(articleId)

      Articles.filter(_.id === articleId).list must be empty
    }

    "returns false when article not exists" in withSession { implicit session: Session =>
      populateDb
      articlesRepository.remove(1000) must beFalse
    }
  }

  def articlesCount(implicit session: Session) = Query(Articles.length).first
}
