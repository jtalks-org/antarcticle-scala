package controllers.filters

import org.specs2.mock.Mockito
import org.specs2.specification.BeforeEach
import play.api.libs.iteratee.Enumerator
import play.api.mvc._
import play.api.test.{FakeHeaders, _}
import play.mvc.Http.HeaderNames
import util.PortFinder

class KeepReferrerFilterSpec extends PlaySpecification with Mockito with BeforeEach {

  var resultRequest: Result = _
  var action: Action[AnyContent] = _
  val fakeApp = FakeApplication(additionalConfiguration = Map("application.secret" -> "test"))
  val port = PortFinder.findFreePort()
  
  def before = {
    resultRequest = spy(Result(header = ResponseHeader(200, Map()), body = mock[Enumerator[Array[Byte]]]))
    action = Action { request => resultRequest }
  }

  "filter" should {
    "set correct referrer to session" in new WithServer(fakeApp, port) {
      val requestUrl = "/articles/new"
      val request = FakeRequest("GET", requestUrl, headers = FakeHeaders(), body = "")

      await(KeepReferrerFilter(action)(request).run)

      there was one(resultRequest).withSession(HeaderNames.REFERER -> requestUrl)
    }

    "not set referrer for javascript resources" in new WithServer(fakeApp, port) {
      val requestUrl = "context/javascripts/default.js"
      val request = FakeRequest("GET", requestUrl, headers = FakeHeaders(), body = "")

      await(KeepReferrerFilter(action)(request).run)

      there was no(resultRequest).withSession(HeaderNames.REFERER -> requestUrl)
    }

    "not set referrer for style resources" in new WithServer(fakeApp, port) {
      val requestUrl = "context/stylesheets/style.css"
      val request = FakeRequest("GET", requestUrl, headers = FakeHeaders(), body = "")

      await(KeepReferrerFilter(action)(request).run)

      there was no(resultRequest).withSession(HeaderNames.REFERER -> requestUrl)
    }

    "not set referrer for images" in new WithServer(fakeApp, port) {
      val requestUrl = "context/images/logo.png"
      val request = FakeRequest("GET", requestUrl, headers = FakeHeaders(), body = "")

      await(KeepReferrerFilter(action)(request).run)

      there was no(resultRequest).withSession(HeaderNames.REFERER -> requestUrl)
    }

    "not set referrer for fonts" in new WithServer(fakeApp, port) {
      val requestUrl = "context/fonts/FontAwesome.otf"
      val request = FakeRequest("GET", requestUrl, headers = FakeHeaders(), body = "")

      await(KeepReferrerFilter(action)(request).run)

      there was no(resultRequest).withSession(HeaderNames.REFERER -> requestUrl)
    }

    "not set referrer for post requests" in new WithServer(fakeApp, port) {
      val requestUrl = "/articles/new"
      val request = FakeRequest("POST", requestUrl, headers = FakeHeaders(), body = "")

      await(KeepReferrerFilter(action)(request).run)

      there was no(resultRequest).withSession(HeaderNames.REFERER -> requestUrl)
    }

    "not set referrer for ajax requests" in new WithServer(fakeApp, port) {
      val requestUrl = "/articles/new"
      val request = FakeRequest("GET", requestUrl,
        headers = FakeHeaders(Seq("X-Requested-With" -> Seq("XMLHttpRequest"))), body = "")

      await(KeepReferrerFilter(action)(request).run)

      there was no(resultRequest).withSession(HeaderNames.REFERER -> requestUrl)
    }
  }
}