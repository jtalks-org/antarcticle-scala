import controllers._
import migrations.{Migrations, MigrationTool}
import services._
import repositories._
import models.database._
import conf.DatabaseConfiguration
import play.api.mvc.Controller
import validators.{ArticleValidator, TagValidator}

object Application
  extends Services
  with Controllers
  with Repositories
  with MigrationTool
  with Schema
  with DatabaseConfiguration {

  override val migrationsContainer = new Migrations(profile)

  withSession { implicit s: scala.slick.session.Session =>
    migrate
  }
}



trait Repositories
  extends SlickArticlesRepositoryComponent
  with TagsRepositoryComponentImpl
  with CommentRepositoryComponentImpl {
  this: Schema with Profile =>
}

trait Services
  extends ArticlesServiceComponentImpl
  with TagsServiceComponentImpl
  with CommentServiceComponentImpl {
  this: Repositories with SessionProvider =>

  override val tagValidator = new TagValidator
  override val articleValidator = new ArticleValidator
}

trait Controllers
  extends Controller
  with ArticleController
  with AuthenticationController
  with UserController
  with CommentController {
  this: Services =>
}

