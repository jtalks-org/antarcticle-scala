package repositories

import org.specs2.mutable._

import models.database._
import utils.DateImplicits._
import org.specs2.time.NoTimeConversions
import com.github.nscala_time.time.Imports._
import util.TestDatabaseConfiguration
import migrations.{MigrationTool, MigrationsContainer}
import org.specs2.specification.BeforeExample
import scala.None

class ArticlesRepositorySpec extends Specification with NoTimeConversions {
  object repository extends TestDatabaseConfiguration with Schema with MigrationTool
      with SlickArticlesRepositoryComponent {
    override val migrationsContainer = new MigrationsContainer {}
  }


  import repository._
  import profile.simple._
  import scala.slick.jdbc.JdbcBackend.Session

  def populateDb(implicit session: Session) = {
    migrate

    val time = DateTime.now
    tags.map(_.name).insertAll("tag1", "tag2", "tag3")
    users.insertAll(UserRecord(None, "user1"), UserRecord(None, "user2"))
    articles.map(a => (a.title, a.content, a.createdAt, a.updatedAt, a.description, a.authorId))
      .insertAll(
        ("New title 1", "<b>content</b>", time + 1.day, time, "description1", 1),
        ("New title 2", "<i>html text</i>", time, time, "description2", 2),
        ("New title 3", "<i>html text</i>", time + 2.days, time, "description3", 2),
        ("New title 4", "<i>html text</i>", time + 4.days, time, "description4", 2)
      )
    articlesTags.map(at => (at.articleId, at.tagId)).insertAll(
       (1,1), (1,2), (2,1), (2,3)
    )
  }

  def withTestDb[T](f: Session => T) = withSession { implicit s: Session =>
    populateDb
    f(s)
  }

  def asArticle(t: (ArticleRecord, UserRecord, Seq[String])) = t._1
  def asAuthor(t: (ArticleRecord, UserRecord, Seq[String])) = t._2
  def asTags(t: (ArticleRecord, UserRecord, Seq[String])) = t._3

  "list portion" should {
    "return portion with size 2" in withTestDb { implicit session: Session =>
      val offset = 0
      val portionSize = 2

      val portion = articlesRepository.getList(offset, portionSize)

      portion must have size 2
    }

    "return portion with offset 1" in withTestDb { implicit session: Session =>
      val thirdArticleId = 3

      val offset = 1
      val portionSize = 2

      val portion = articlesRepository.getList(offset, portionSize)

      val firstArticleInPortion = asArticle(portion(0))
      firstArticleInPortion.id must beSome(thirdArticleId)
    }

    "be sorted by creation time" in withTestDb { implicit session: Session =>
      val portion = articlesRepository.getList(0, 4)

      portion.map(asArticle(_).id.get) must contain(allOf(4, 3, 1, 2).inOrder)
    }

    "return article tags" in withTestDb { implicit session: Session =>
      val portion = articlesRepository.getList(0, 2)

      asTags(portion(0)) must have size 2
      asTags(portion(1)) must have size 2
    }

    "return article author" in withTestDb { implicit session: Session =>
      val portion = articlesRepository.getList(0, 2)

      asAuthor(portion(0)).id must beSome(2)
      asAuthor(portion(1)).id must beSome(2)
    }
  }

  "list portion for user" should {
    "return articles created by user2" in withTestDb { implicit session: Session =>
      val userId = 2

      val portion = articlesRepository.getListForUser(userId, 0, 10)

      portion.map(asAuthor(_).id) must contain((id: Option[Int]) => id must beSome(userId))
    }
  }


  "article by id" should {
    "return article with id 2" in withTestDb { implicit session: Session =>
      val article = articlesRepository.get(2)

      article must beSome
      asArticle(article.get).id must beSome(2)
    }

    "return None when there are no article with id 2000" in withTestDb { implicit session: Session =>
      val article = articlesRepository.get(2000)

      article must beNone
    }
  }

  "inserting new article" should {
    "inserts new article record" in withTestDb { implicit session: Session =>
      val oldCount = articlesCount

      val userId = 2
      val newArticle = ArticleRecord(None, "test article", "content", DateTime.now, DateTime.now, "descr", userId)

      articlesRepository.insert(newArticle)

      articlesCount must_== (oldCount + 1)
    }

    "assigns id to new article" in withTestDb { implicit session: Session =>
      val userId = 2
      val newArticle = ArticleRecord(None, "test article", "content", DateTime.now, DateTime.now, "descr", userId)

      val insertedArticleId: Int = articlesRepository.insert(newArticle)
      true
    }
  }

  "updating article" should {
    "update existing article" in withTestDb { implicit session: Session =>
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

    "return false when updating not existing article" in withTestDb { implicit session: Session =>
      val upd = ArticleToUpdate("title", "content", DateTime.now, "desc")

      articlesRepository.update(2000, upd) must beFalse
    }
  }

  "removing article" should {
    "remove article" in withTestDb { implicit session: Session =>
      val oldCount = articlesCount

      articlesRepository.remove(2) must beTrue
      articlesCount must_== (oldCount - 1)
    }

    "remove associated tags" in withTestDb { implicit session: Session =>
      val articleId = 1

      articlesRepository.remove(articleId)

      articlesTags.filter(_.articleId === articleId).list must be empty
    }

    "remove expected article" in withTestDb { implicit session: Session =>
      val articleId = 2

      articlesRepository.remove(articleId)

      articles.filter(_.id === articleId).list must be empty
    }

    "return false when article not exists" in withTestDb { implicit session: Session =>
      articlesRepository.remove(1000) must beFalse
    }
  }

  "articles count" should {
    "return articles count" in withTestDb { implicit session: Session =>

      val count = articlesRepository.count

      count must_== 4
    }
  }

  "articles count by author" should {
    "return articles count for user2" in withTestDb { implicit session: Session =>
      val userId = 2

      val count = articlesRepository.countForUser(userId)

      count must_== 3
    }
  }

  def articlesCount(implicit session: Session) = articles.length.run
}
