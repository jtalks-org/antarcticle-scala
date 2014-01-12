import controllers._
import migrations.{Migrations, MigrationTool}
import security._
import services._
import repositories._
import models.database._
import conf.{JndiPropertiesProviderComponent, DatabaseConfiguration}
import play.api.mvc.Controller
import validators.{ArticleValidator, TagValidator}

object Application
  extends JndiPropertiesProviderComponent
  with DatabaseConfiguration
  with Schema
  with MigrationTool
  with Repositories
  with SecurityComponent
  with Services
  with Controllers {

  override val migrationsContainer = new Migrations(profile)

  withSession { implicit s: scala.slick.session.Session =>
    migrate
  }
}

trait Controllers
  extends Controller
  with ArticleController
  with AuthenticationController
  with UserController
  with CommentController {
  this: Services with SecurityComponent =>
}

trait Services
  extends ArticlesServiceComponentImpl
  with TagsServiceComponentImpl
  with CommentServiceComponentImpl {
  this: Repositories with SessionProvider =>

  override val tagValidator = new TagValidator
  override val articleValidator = new ArticleValidator
}

trait Repositories
  extends SlickArticlesRepositoryComponent
  with TagsRepositoryComponentImpl
  with CommentRepositoryComponentImpl
  with UsersRepositoryComponentImpl {
  this: Schema with Profile =>
}
