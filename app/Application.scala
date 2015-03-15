import conf.ConfigurationComponent
import controllers._
import jobs.Scheduler
import migrations.{MigrationTool, Migrations}
import models.database._
import play.api.mvc.Controller
import repositories._
import security._
import services._
import validators.{ArticleValidator, TagValidator, UserValidator}

object Application
  extends ConfigurationComponent
  with Schema
  with MigrationTool
  with Repositories
  with SecurityComponent
  with Services
  with Controllers
  with PropertiesProvider
  with MailServiceComponentImpl
  with Scheduler {

  override val migrationsContainer = new Migrations(profile)
  override val userValidator = new UserValidator

  lazy val instanceName = propertiesService.getInstanceName()

  withSession { implicit session =>
    migrate
  }
  runJobs()
}

trait Controllers
  extends Controller
  with ArticleController
  with AuthenticationController
  with UserController
  with HelpController
  with NotificationsController
  with CommentController
  with ApplicationPropertiesController
  with TagsController {
  this: Services with SecurityComponent with PropertiesProvider =>
}

trait Services
  extends ArticlesServiceComponentImpl
  with TagsServiceComponentImpl
  with CommentsServiceComponentImpl
  with NotificationsServiceComponentImpl
  with UsersServiceComponentImpl
  with ApplicationPropertiesServiceComponentImpl {
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
  with ApplicationPropertiesRepositoryComponentImpl {
  this: Schema with Profile =>
}
