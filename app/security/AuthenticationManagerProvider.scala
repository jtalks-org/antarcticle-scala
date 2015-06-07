package security

import conf.Keys.{PoulpeSecret, PoulpeUsername}
import conf.{Keys, PropertiesProviderComponent}
import models.database.UserRecord
import play.api.Logger
import repositories.UsersRepositoryComponent
import security.UserInfoImplicitConversions._
import services.{ActorSystemProvider, SessionProvider}
import utils.SecurityUtil

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Future, Promise}
import scala.slick.jdbc.JdbcBackend
import scala.xml.NodeSeq
import scalaz.Scalaz._
import scalaz._

trait AuthenticationManagerProvider {
  def authenticationManager: AuthenticationManager

  trait AuthenticationManager {
    def authenticate(username: String, password: String): Future[Option[UserInfo]]
    def activate(uuid: String): Future[ValidationNel[String, Unit]]
    def register(user: UserInfo): Future[ValidationNel[String, String]]
  }
}

trait AuthenticationManagerProviderImpl extends AuthenticationManagerProvider {
  this: UsersRepositoryComponent with SessionProvider with ActorSystemProvider with PropertiesProviderComponent =>

  import spray.http._
  import HttpMethods._
  import spray.client.pipelining._
  import spray.httpx.unmarshalling._

  override val authenticationManager = new CompositeAuthenticationManager()

  class LocalDatabaseAuthenticationManager extends AuthenticationManager {
    override def authenticate(username: String, password: String): Future[Option[UserInfo]] = {
      def isValidPassword(user: UserRecord) = {
        !password.isEmpty && user.password === SecurityUtil.encodePassword(password, user.salt)
      }
      Future {
        withSession {
          implicit s: JdbcBackend#Session => {
            val user : Option[UserInfo] = for {
              record <- usersRepository.getByUsername(username) if isValidPassword(record)
            } yield record
            user
          }
        }
      }
    }

    override def register(user: UserInfo) = withSession {
      implicit s: JdbcBackend#Session =>

        def checkUsernameUnique = usersRepository.getByUsername(user.username).cata(
          existingUser => s"User with the username ${user.username} already exists".failureNel,
          ().successNel
        )
        def checkEmailUnique = usersRepository.getByEmail(user.email).cata(
          existingUser => s"User with the email ${user.email} already exists".failureNel,
          ().successNel
        )

        Future.successful {(checkUsernameUnique |@| checkEmailUnique) {
          case _ => SecurityUtil.generateUid
        }}
    }

    override def activate(uuid: String): Future[ValidationNel[String, Unit]] = withSession {
      implicit s: JdbcBackend#Session =>
        Future {
          usersRepository.getByUID(uuid).cata(
            user => if (!user.active) ().successNel else "User is already activated".failureNel,
            none = "There is no user with such uid".failureNel
          )
        }
    }
  }

  trait PipeProvider {
    def pipeline: HttpRequest => Future[HttpResponse]
  }

  trait PipeProviderImpl extends PipeProvider {
    implicit val system = actorSystem
    override def pipeline: HttpRequest => Future[HttpResponse] = sendReceive
  }

  class PoulpeAuthenticationManager(poulpeUrl: String) extends AuthenticationManager {
    this : PipeProvider =>

    val genericError = "Some unexpected error occurred, please contact administrator or try later"

    val poulpeCredentials = for {
      poulpeUsername <- propertiesProvider.get(PoulpeUsername)
      poulpeSecret <- propertiesProvider.get(PoulpeSecret)
    } yield BasicHttpCredentials(poulpeUsername, poulpeSecret)

    val addPoulpeCredentials: HttpRequest => HttpRequest = { request =>
      poulpeCredentials match {
        case Some(credentials) => request ~> addCredentials(credentials)
        case None =>
          Logger.warn("Poulpe credentials are not set")
          request
      }
    }

    implicit val errorsUnmarshaller: Unmarshaller[List[String]] =
      Unmarshaller.delegate[NodeSeq, List[String]](MediaTypes.`text/xml`, MediaTypes.`application/xml`) {
        nodeSeq => (nodeSeq \ "error").map(x => (x \ "@code").text).toList
      }

    implicit val userInfoUnmarshaller: Unmarshaller[Option[UserInfo]] =
      Unmarshaller.delegate[NodeSeq, Option[UserInfo]](MediaTypes.`text/xml`, MediaTypes.`application/xml`) {
        xmlResponse => {
          for {
            status <- (xmlResponse \\ "status").headOption.map(_.text) if status == "success"
            enabled <- (xmlResponse \\ "enabled").headOption.map(_.text) if enabled == "true"
            returnedUsername <- (xmlResponse \\ "username").headOption.map(_.text)
            email <- (xmlResponse \\ "email").headOption.map(_.text)
            firstName = (xmlResponse \\ "firstName").headOption.map(_.text)
            lastName = (xmlResponse \\ "lastName").headOption.map(_.text)
            password = (xmlResponse \\ "password").headOption.map(_.text)
          } yield {
            UserInfo(returnedUsername, "todo", email, firstName, lastName, active = true)
          }
        }
      }

    override def authenticate(username: String, password: String): Future[Option[UserInfo]] = {
      val request = HttpRequest(GET, Uri(s"$poulpeUrl/rest/authenticate").withQuery(
        "username" -> username, "passwordHash" -> SecurityUtil.md5(password)
      ))
      pipeline(request).map { response =>
        response.status match {
          case StatusCodes.OK =>
            response.entity.as[Option[UserInfo]] match {
              case Left(e) => throw new Exception("Could not deserialize response")
              case Right(u) => u
            }
          case _ => None
        }
      }
    }

    override def register(user: UserInfo): Future[ValidationNel[String, String]] = {
      val data =
        s"""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
        <user xmlns="http://www.jtalks.org/namespaces/1.0">
          <username>${user.username}</username>
          <passwordHash>${user.password}</passwordHash>
          <email>${user.email}</email>
        </user>"""

      val request = HttpRequest(POST, Uri(s"$poulpeUrl/rest/private/user"))
        .withEntity(HttpEntity(MediaTypes.`text/xml`, data))

      def translate(keys : List[String], user: UserInfo): List[String] = {
        keys.foldRight(List[String]()) { (key, errors) =>
          key match {
            case "user.username.already_exists" => s"User with the username ${user.username} already exists" :: errors
            case "user.email.already_exists" => s"User with the email ${user.email} already exists" :: errors
            case _ => errors
          }
        }
      }

      def deserialize(result: Deserialized[List[String]]) = {
        result match {
          case Right(errors) =>
            translate(errors, user) match {
              case head :: tail => Failure(NonEmptyList(head, tail: _*))
              case Nil => genericError.failureNel
            }
          case Left(e) => genericError.failureNel
        }
      }

      pipeline(request ~> addPoulpeCredentials).map { response =>
        response.status match {
          case StatusCodes.OK =>
            Logger.info(s"Registration request to poulpe was successful with http status ${response.status} " +
              s"and response ${response.entity.asString}")
            response.entity.asString.trim().successNel
          case StatusCodes.BadRequest =>
            Logger.info(s"Registration request to poulpe failed with http status ${response.status} " +
              s"and response ${response.entity.asString}")
            deserialize(response.entity.as[List[String]])
          case _ =>
            Logger.warn(s"Registration request to poulpe failed with http status ${response.status} " +
              s"and response ${response.entity.asString}")
            genericError.failureNel
        }
      }
    }

    override def activate(uuid: String): Future[ValidationNel[String, Unit]] = {
      val request = HttpRequest(GET, Uri(s"$poulpeUrl/rest/private/activate").withQuery("uuid" -> uuid))
      pipeline(request ~> addPoulpeCredentials).map { response =>
        response.status match {
          case status if List(StatusCodes.OK, StatusCodes.NoContent).any(_ == status)  => ().successNel
          case status if List(StatusCodes.NotFound, StatusCodes.BadRequest).any(_ == status) =>
            response.entity.as[List[String]] match {
              case Right(head :: tail) => Failure(NonEmptyList(head, tail: _*))
              case _ => genericError.failureNel
            }
        }
      }
    }
  }

  class FakeAuthenticationManager extends AuthenticationManager {

    val notSupported: ValidationNel[String, Nothing] = "Not supported by fake AuthenticationManager".failureNel

    override def authenticate(username: String, password: String) = Future.successful {
      (username, password) match {
        case ("admin", "admin") => some {
          UserInfo("admin", password, "email@email.com", "firstName".some, "lastName".some, active = true)
        }
        case _ => none[UserInfo]
      }
    }

    override def register(user: UserInfo) = Future.successful(notSupported)

    override def activate(uuid: String) = Future.successful(notSupported)
  }

  class CompositeAuthenticationManager extends AuthenticationManager {

    lazy val poulpeAuthManager: Option[AuthenticationManager] = {
      propertiesProvider.get(Keys.UseFakeAuthentication) match {
        case Some(true) => Some(new FakeAuthenticationManager)
        case _ =>
          propertiesProvider.get(Keys.PoulpeUrl) match {
            case Some(url) if !url.isEmpty =>
              Logger.warn(s"Using Poulpe authentication manager with Poulpe at $url")
              Some(new PoulpeAuthenticationManager(url) with PipeProviderImpl)
            case _ =>
              Logger.warn("Using local database authentication manager")
              None
          }
      }
    }

    lazy val localAuthManager: AuthenticationManager = new LocalDatabaseAuthenticationManager

    override def authenticate(username: String, password: String) = {
      val p = Promise[Option[UserInfo]]()
      poulpeAuthManager.cata(
        none = localAuthManager.authenticate(username, password),
        some = manager => manager.authenticate(username, password)).onComplete {
        case s@scala.util.Success(x) => p.tryComplete(s)
        case scala.util.Failure(e) =>
          Logger.error("Error while asking Poulpe to authenticate user", e)
          p.completeWith(localAuthManager.authenticate(username, password))
      }
      p.future
    }

    override def register(user: UserInfo) = {
      poulpeAuthManager.cata(
        some = manager => {
          manager.register(user)
        },
        none = localAuthManager.register(user)
      )
    }

    override def activate(uuid: String) = {
      poulpeAuthManager.cata(
        some = manager => {
          manager.activate(uuid)
        },
        none = localAuthManager.activate(uuid)
      )
    }
  }
}

