package repositories

import org.specs2.mutable._

import models.database._
import utils.Implicits._
import org.specs2.time.NoTimeConversions
import com.github.nscala_time.time.Imports._
import util.TestDatabaseConfigurationWithFixtures

import migrations.{MigrationTool, MigrationsContainer}

class ArticlesRepositorySpec extends Specification with NoTimeConversions {
  object repository extends TestDatabaseConfigurationWithFixtures with Schema
    with SlickArticlesRepositoryComponent


  import repository._
  import profile.simple._

  def asArticle(t: (ArticleRecord, UserRecord, Seq[String], Int)) = t._1
  def asAuthor(t: (ArticleRecord, UserRecord, Seq[String], Int)) = t._2
  def asTags(t: (ArticleRecord, UserRecord, Seq[String], Int)) = t._3
  def asCommentsCount(t: (ArticleRecord, UserRecord, Seq[String], Int)) = t._4

  "list portion" should {
    "return portion with size 2" in withTestDb { implicit session =>
      val offset = 0
      val portionSize = 2

      val portion = articlesRepository.getList(offset, portionSize, None)

      portion must have size 2
    }

    "return portion with offset 1" in withTestDb { implicit session =>
      val thirdArticleId = 3

      val offset = 1
      val portionSize = 2

      val portion = articlesRepository.getList(offset, portionSize, None)

      val firstArticleInPortion = asArticle(portion(0))
      firstArticleInPortion.id must beSome(thirdArticleId)
    }

    "be sorted by creation time" in withTestDb { implicit session =>
      val portion = articlesRepository.getList(0, 4, None)

      portion.map(asArticle(_).id.get) must contain(allOf(4, 3, 1, 2).inOrder)
    }

    "return article tags" in withTestDb { implicit session =>
      val portion = articlesRepository.getList(0, 4, None)

      portion.map(asTags(_).length) must_== Seq(0, 0, 2, 2)
    }

    "return article author" in withTestDb { implicit session =>
      val portion = articlesRepository.getList(0, 4, None)

      portion.map(asAuthor(_).id.get) must_== Seq(2, 2, 1, 2)
    }

    "return articles tagged with tag1" in withTestDb { implicit session =>
      val portion = articlesRepository.getList(0, 10, Some(Seq(1)))

      portion.map(asTags(_).contains("tag1")) must_== Seq(true, true)
    }

    "return articles tagged with tag1 AND tag2" in withTestDb { implicit session =>
      val portion = articlesRepository.getList(0, 10, Some(Seq(1,2)))

      portion.map(asTags(_).contains("tag1")) must_== Seq(true)
      portion.map(asTags(_).contains("tag2")) must_== Seq(true)
    }

    "return limited number of articles with count of comments" in withTestDb { implicit session =>
      val portion = articlesRepository.getList(0, 4, None)

      portion.map(asCommentsCount(_)) must_== Seq(1, 1, 4, 2)
    }
  }

 "list portion for user" should {
   "return articles created by user2" in withTestDb { implicit session =>
     val userId = 2

     val portion = articlesRepository.getListForUser(userId, 0, 10, None)

     portion.map(asAuthor(_).id) must contain((id: Option[Int]) => id must beSome(userId))
   }

   "return articles tagged with tag1 by user2" in withTestDb { implicit session =>
     val userId = 2
     val tagId = 1
     val tagName = "tag1"

     val portion = articlesRepository.getListForUser(userId, 0, 10, Some(Seq((tagId))))

     portion.map(asTags(_).contains(tagName)) must_== Seq(true)
   }

   "return limited number of articles with count of comments for specified user" in withTestDb { implicit session =>
     val userId = 2

     val portion = articlesRepository.getListForUser(userId, 0, 3, None)

     portion.map(asCommentsCount(_)) must_== Seq(1, 1, 2)
   }
 }


  "article by id" should {
    "return article with id 2" in withTestDb { implicit session =>
      val article = articlesRepository.get(2)

      article must beSome
      article.get._1.id must beSome(2)
    }

    "return None when there are no article with id 2000" in withTestDb { implicit session =>
      val article = articlesRepository.get(2000)

      article must beNone
    }
  }

  "inserting new article" should {
    "inserts new article record" in withTestDb { implicit session =>
      val oldCount = articlesCount

      val userId = 2
      val newArticle = ArticleRecord(None, "test article", "content", DateTime.now, DateTime.now, "descr", userId)

      articlesRepository.insert(newArticle)

      articlesCount must_== (oldCount + 1)
    }

    "assigns id to new article" in withTestDb { implicit session =>
      val userId = 2
      val newArticle = ArticleRecord(None, "test article", "content", DateTime.now, DateTime.now, "descr", userId)

      val insertedArticleId: Int = articlesRepository.insert(newArticle)
      true
    }
  }

  "updating article" should {
    "update existing article" in withTestDb { implicit session =>
      val updatedArticleId = 2
      val articleToBeUpdated = articles.filter(_.id === updatedArticleId).first

      val newContent = "new content"
      val upd = ArticleToUpdate(articleToBeUpdated.title,
        newContent, articleToBeUpdated.createdAt, articleToBeUpdated.description)

      //TODO: split assertions
      articlesRepository.update(updatedArticleId, upd) must beTrue
      val actualContent = articles.filter(_.id === updatedArticleId).map(_.content).first
      actualContent must_== newContent
    }

    "return false when updating not existing article" in withTestDb { implicit session =>
      val upd = ArticleToUpdate("title", "content", DateTime.now, "desc")

      articlesRepository.update(2000, upd) must beFalse
    }
  }

  "removing article" should {
    "remove article" in withTestDb { implicit session =>
      val oldCount = articlesCount

      articlesRepository.remove(2) must beTrue
      articlesCount must_== (oldCount - 1)
    }

    "remove associated tags" in withTestDb { implicit session =>
      val articleId = 1

      articlesRepository.remove(articleId)

      articlesTags.filter(_.articleId === articleId).list must be empty
    }

    "remove expected article" in withTestDb { implicit session =>
      val articleId = 2

      articlesRepository.remove(articleId)

      articles.filter(_.id === articleId).list must be empty
    }

    "return false when article not exists" in withTestDb { implicit session =>
      articlesRepository.remove(1000) must beFalse
    }
  }

  "articles count" should {
    "return articles count" in withTestDb { implicit session: Session =>
      articlesRepository.count(None) must_== 4
    }

    "return articles count for certain tag" in withTestDb { implicit session: Session =>
      println(articlesRepository.count(Some(Seq(1))))
      articlesRepository.count(Some(Seq(1))) must_== 2
    }
  }

  "articles count by author" should {
    "return articles count for user2" in withTestDb { implicit session =>
      val userId = 2

      val count = articlesRepository.countForUser(userId)

      count must_== 3
    }
  }

  def articlesCount(implicit session: Session) = articles.length.run
}
