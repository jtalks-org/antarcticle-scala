import controllers._
import migrations.{Migrations, MigrationTool}
import security._
import services._
import repositories._
import models.database._
import conf.ConfigurationComponent
import play.api.mvc.Controller
import validators.{ArticleValidator, TagValidator}

object Application
  extends ConfigurationComponent
  with Schema
  with MigrationTool
  with Repositories
  with SecurityComponent
  with Services
  with Controllers {

  override val migrationsContainer = new Migrations(profile)

  lazy val instanceName = propertiesService.getInstanceName()

  withSession { implicit session =>
    migrate
  }
}

trait Controllers
  extends Controller
  with ArticleController
  with AuthenticationController
  with UserController
  with HelpController
  with NotificationsController
  with CommentController
  with TagsController
  with PropertiesController {
  this: Services with SecurityComponent =>
}

trait Services
  extends ArticlesServiceComponentImpl
  with TagsServiceComponentImpl
  with CommentsServiceComponentImpl
  with NotificationsServiceComponentImpl
  with UsersServiceComponentImpl
  with PropertiesServiceComponentImpl {
  this: Repositories with SessionProvider =>

  override val tagValidator = new TagValidator
  override val articleValidator = new ArticleValidator(tagValidator)
}

trait Repositories
  extends SlickArticlesRepositoryComponent
  with TagsRepositoryComponentImpl
  with CommentsRepositoryComponentImpl
  with NotificationsRepositoryComponentImpl
  with UsersRepositoryComponentImpl
  with PropertiesRepositoryComponentImpl {
  this: Schema with Profile =>
}
