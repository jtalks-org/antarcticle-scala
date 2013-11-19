package org.jtalks.antarcticle

import org.jtalks.antarcticle.persistence.repositories.SlickArticlesRepositoryComponent
import org.jtalks.antarcticle.persistence._
import scala.slick.driver.{H2Driver, ExtendedProfile}
import scala.slick.session.{Session, Database}
import org.scalatest.{BeforeAndAfter, FunSuite}
import org.jtalks.antarcticle.persistence.User
import org.jtalks.antarcticle.persistence.Article
import java.sql.Timestamp
import org.scalatest.matchers.ShouldMatchers

class ArticlesRepositoryTest extends FunSuite with ShouldMatchers with BeforeAndAfter {
  val database = Database.forURL("jdbc:h2:mem:test1", driver = "org.h2.Driver")

  val repository = new SlickArticlesRepositoryComponent with UsersComponent with ArticlesComponent with Profile with DatabaseProvider {
    override val profile: ExtendedProfile = H2Driver
    override val db = database
    val schema = Articles.ddl ++ Users.ddl
  }

  import repository._
  import repository.profile.simple._

  test("findAll") {
    withDb { implicit session: Session =>
      val time = new Timestamp(new java.util.Date().getTime())

      Users.insertAll(
        User(None, "user1"),
        User(None, "user2")
      )

      Articles.insertAll(
        Article(None, "New title", "<b>content</b>", time, 1),
        Article(None, "New title 2", "<i>html text</i>", time, 2)
      )

      val articles = articlesRepository.findAll
      articles should contain (ArticleListModel(1, "New title", "<b>content</b>", time, UserModel(1,"user1")))
    }
  }


  test("get") {
    withDb { implicit session: Session =>
      val time = new Timestamp(new java.util.Date().getTime())

      Users.insertAll(
        User(None, "user1"),
        User(None, "user2")
      )

      Articles.insertAll(
        Article(None, "New title", "<b>content</b>", time, 1),
        Article(None, "New title 2", "<i>html text</i>", time, 2)
      )

      val article = articlesRepository.get(1)
      article.get should be (ArticleListModel(1, "New title", "<b>content</b>", time, UserModel(1,"user1")))
    }
  }

  def withDb[T](f: Session => T): T = {
    database.withSession { implicit session: Session =>
      try {
        repository.schema.create
        f(session)
      } finally {
        repository.schema.drop
      }
    }
  }

}
