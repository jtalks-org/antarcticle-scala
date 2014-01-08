import controllers._
import migrations.{Migrations, MigrationTool}
import security.SecurityServiceComponentImpl
import services._
import repositories._
import models.database._
import conf.DatabaseConfiguration
import play.api.mvc.Controller
import validators.{ArticleValidator, TagValidator}
import security.{UUIDTokenProvider , FakeAuthenticationManager, Authentication}

object Application
  extends Controllers
  with Services
  with Security
  with Repositories
  with MigrationTool
  with Schema
  with DatabaseConfiguration {

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
  this: Services with Security =>
}

trait Services
  extends ArticlesServiceComponentImpl
  with TagsServiceComponentImpl
  with CommentServiceComponentImpl {
  this: Repositories with SessionProvider =>

  override val tagValidator = new TagValidator
  override val articleValidator = new ArticleValidator
}

trait Security extends SecurityServiceComponentImpl with Authentication {
  this: Controller with UsersRepositoryComponent with SessionProvider =>

  override val tokenProvider = new UUIDTokenProvider
  override val authenticationManager = new FakeAuthenticationManager
}

trait Repositories
  extends SlickArticlesRepositoryComponent
  with TagsRepositoryComponentImpl
  with CommentRepositoryComponentImpl
  with UsersRepositoryComponentImpl {
  this: Schema with Profile =>
}
