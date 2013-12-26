package controllers

import play.api._
import play.api.mvc._
import services._
import repositories.{Repositories, SlickArticlesRepositoryComponent}
import models.database._
import scala.slick.driver.{MySQLDriver, ExtendedProfile, PostgresDriver, H2Driver}
import scala.slick.session.Database
import models.database.UserToInsert
import models.database.ArticleToInsert
import conf.{DatabaseConfiguration, JndiProperties}
import scala.util.Try
import javax.naming.NamingException

trait Backend extends ArticlesServiceComponentImpl
  with SlickArticlesRepositoryComponent with UsersSchemaComponent
  with ArticlesSchemaComponent with CommentsSchemaComponent with Profile with SlickSessionProvider {

  val db = scala.slick.session.Database.forURL(url = "jdbc:postgresql:antarcticle",
    user = "postgres", password = "postgres", driver = "org.postgresql.Driver")
  override val profile = PostgresDriver

  {
    import profile.simple._
    import com.github.nscala_time.time.Imports._
    import utils.DateImplicits._
    import scala.slick.jdbc.meta.MTable
    withSession { implicit s: Session =>
      if (MTable.getTables.list().isEmpty) {
        (Articles.ddl ++ Users.ddl ++ Comments.ddl).create
        val time = DateTime.now
        val users = List(
          UserToInsert("user1"),
          UserToInsert("user2"),
          UserToInsert("user3")
        )
        val articles = List(
          ArticleToInsert("New title 1", "<b>content</b>", time + 1.day, time, "description1", 1),
          ArticleToInsert("New title 2", "<i>html text</i>", time, time, "description2", 2),
          ArticleToInsert("New title 3", "<i>html text</i>", time + 2.days, time, "description3", 2),
          ArticleToInsert("New title 4", "<i>html text</i>", time - 4.days, time, "description4", 2),
          ArticleToInsert("New title 5", "<i>html text</i>", time + 4.hours, time, "description5", 1),
          ArticleToInsert("New title 6", "<i>html text</i>", time + 40.days, time, "description6", 1),
          ArticleToInsert("New title 7", "<i>html text</i>", time - 4.days, time, "description7", 2),
          ArticleToInsert("New title 8", "<i>html text</i>", time + 10.days, time, "description8", 2),
          ArticleToInsert("New title 9", "<i>html text</i>", time - 4.days, time, "description9", 2),
          ArticleToInsert("New title 10", "<i>html text</i>", time + 2.days, time, "description10", 2)
        )

        Users.forInsert.insertAll(users : _*)
        Articles.forInsert.insertAll(articles: _*)
      }
    }
  }
}

trait ArticlesController {
  this: Controller with ArticlesServiceComponent =>

  def index = Action {
    Redirect(routes.ArticlesController.articles(0))
  }

  def articles(page: Int) = Action {
    Ok(views.html.main("Articles")(views.html.articles(articlesService.getPage(page), page)))
  }
}

object Application extends Services with Controllers with Repositories with Schema with DatabaseConfiguration
