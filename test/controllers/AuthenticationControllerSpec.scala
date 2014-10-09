package controllers

import conf.PropertiesProviderComponent
import org.specs2.mutable.Specification
import org.specs2.mock.Mockito
import org.specs2.specification.AfterExample
import util.{FakePropertiesProvider, FakeAuthentication}
import play.api.test._
import play.api.test.Helpers._
import scalaz._
import Scalaz._
import security.SecurityServiceComponent
import scala.concurrent.Future
import conf.Constants._
import security.AuthenticatedUser
import play.api.mvc.Cookie
import scala.Some
import org.mockito.Matchers
import services.ApplicationPropertiesServiceComponent

class AuthenticationControllerSpec extends Specification with Mockito with AfterExample {

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

  "show login page" should {

    "render login page" in {
      val page = controller.showLoginPage(FakeRequest())

      status(page) must equalTo(200)
      contentType(page) must beSome("text/html")
    }
  }

  "login form submit" should {

    val username = "username"
    val password = "password"
    val referer = "/articles/new"
    val rememberMeToken = "token"
    val user = mock[AuthenticatedUser]
    val request = FakeRequest("POST","/")
      .withFormUrlEncodedBody(("login", username),("password", password), ("referer", referer))

    "perform authentication with valid credentials" in {
      securityService.signInUser(username, password) returns (rememberMeToken, user).successNel

      val page = controller.login(request)

      status(page) must equalTo(200)
      contentType(page) must beSome("text/plain")
      cookies(page).get(rememberMeCookie).get must equalTo(
        Cookie(rememberMeCookie, rememberMeToken, Some(rememberMeExpirationTime), httpOnly = true)
      )
    }

    "return an error if credentials are invalid" in {
      securityService.signInUser(username, password) returns "Invalid credentials".failureNel

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
}
