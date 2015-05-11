package controllers

import conf.Constants._
import models.UserModels.User
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import org.specs2.specification.AfterEach
import play.api.mvc.Cookie
import play.api.test.Helpers._
import play.api.test._
import security.{AuthenticatedUser, SecurityServiceComponent}
import services.ApplicationPropertiesServiceComponent
import util.{FakeAuthentication, FakePropertiesProvider}

import scala.concurrent.Future
import scalaz.Scalaz._

class AuthenticationControllerSpec extends Specification with Mockito with AfterEach {

  object controller extends AuthenticationController
                     with SecurityServiceComponent
                     with ApplicationPropertiesServiceComponent
                     with FakeAuthentication
                     with PropertiesProvider
                     with FakePropertiesProvider {
    override val securityService = mock[SecurityService]
    override val usersRepository = mock[UsersRepository]
    override val propertiesService = mock[ApplicationPropertiesService]
  }

  import controller._

  def after = {
    org.mockito.Mockito.reset(securityService)
    org.mockito.Mockito.reset(usersRepository)
  }

  val username = "username"
  val password = "password"
  val rememberMeToken = "token"

  "show login page" should {

    "render login page" in {
      val page = controller.showLoginPage(FakeRequest())

      status(page) must equalTo(200)
      contentType(page) must beSome("text/html")
    }
  }

  "show signup page" should {

    "render signup page" in {
      val page = controller.showRegistrationPage(FakeRequest())

      status(page) must equalTo(200)
      contentType(page) must beSome("text/html")
    }
  }

  "login form submit" should {

    val referer = "/articles/new"
    val user = mock[AuthenticatedUser]
    val request = FakeRequest("POST","/")
      .withFormUrlEncodedBody(("login", username),("password", password), ("referer", referer))

    "perform authentication with valid credentials" in {
      securityService.signInUser(username, password) returns Future.successful((rememberMeToken, user).successNel)

      val page = controller.login(request)

      status(page) must equalTo(200)
      contentType(page) must beSome("text/plain")
      cookies(page).get(rememberMeCookie).get must equalTo(
        Cookie(rememberMeCookie, rememberMeToken, Some(rememberMeExpirationTime), httpOnly = true)
      )
    }

    "return an error if credentials are invalid" in {
      securityService.signInUser(username, password) returns Future.successful("Invalid credentials".failureNel)

      val page = controller.login(request)

      status(page) must equalTo(400)
      contentType(page) must beSome("text/html")
      cookies(page).get(rememberMeCookie) must beNone
    }

  }

  "logout action" should {

    "terminate current session" in {
      val request = FakeRequest().withCookies(Cookie(rememberMeCookie, "token"))
      val page = controller.logout(request)

      status(page) must equalTo(303)
      // cookie is destroyed by setting negative maxAge, so browser should drop it
      cookies(page).get(rememberMeCookie).get.maxAge.get must beLessThan(0)
    }
  }

  "register action" should {

    val email = "fake@email.com"
    val uuid = "fake-uuid"
    val request = FakeRequest("POST","/")
      .withFormUrlEncodedBody(("login", username),("password", password), ("email", email))

    "after successful registration return home page url" in {
      securityService.signUpUser(any[User], anyString) returns Future.successful(uuid.successNel)

      val page = controller.register(request)
      status(page) must equalTo(200)
      contentType(page) must beSome("text/plain")
    }

    "show error page in case of failed registration" in {
      securityService.signUpUser(any[User], anyString) returns Future.successful("error".failureNel)

      val page = controller.register(request)

      status(page) must equalTo(400)
      contentType(page) must beSome("text/html")
    }
  }

  "activate action" should {

    val uid = "fake-uuid"
    val request = FakeRequest("GET","/activate/" + uid)

    "perform authentication based on uid" in {
      securityService.activateUser(uid) returns Future.successful(rememberMeToken.successNel)
      val page = controller.activate(uid)(request)
      redirectLocation(page) must beSome.which(_ == "/")
      cookies(page).get(rememberMeCookie).get must equalTo(
        Cookie(rememberMeCookie, rememberMeToken, Some(rememberMeExpirationTime), httpOnly = true)
      )
    }

    "redirect to main page in case of failed activation" in {
      securityService.activateUser(uid) returns Future.successful("error".failureNel)
      val page = controller.activate(uid)(request)
      redirectLocation(page) must beSome.which(_ == "/")
    }

  }
}
