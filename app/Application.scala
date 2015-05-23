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

object FailedApplication extends IndexController with WebJarControllerImpl with Controller

object Application
  extends ConfigurationComponent
  with Schema
  with MigrationTool
  with Repositories
  with Services
  with AuthenticationManagerProviderImpl
  with Controllers
  with Authentication
  with PropertiesProvider
  with MailServiceComponentImpl
  with PlayActorSystemProvider
  with Scheduler {

  override val migrationsContainer = new Migrations(profile)

  lazy val instanceName = propertiesService.getInstanceName

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
  with TagsController
  with WebJarControllerImpl {
  this: Services with Authentication with SecurityServiceComponent with PropertiesProvider =>
}

trait Services
  extends ArticlesServiceComponentImpl
  with TagsServiceComponentImpl
  with CommentsServiceComponentImpl
  with NotificationsServiceComponentImpl
  with UsersServiceComponentImpl
  with SecurityServiceComponentImpl
  with ApplicationPropertiesServiceComponentImpl {
  this: Repositories
    with SessionProvider
    with AuthenticationManagerProvider
    with MailServiceComponent =>

  override val tagValidator = new TagValidator
  override val articleValidator = new ArticleValidator(tagValidator)
  override val userValidator = new UserValidator
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
