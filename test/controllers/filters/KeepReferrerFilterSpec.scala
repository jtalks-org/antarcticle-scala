package controllers.filters

import play.api.test._
import play.api.mvc._
import org.specs2.mock.Mockito
import play.mvc.Http.HeaderNames
import play.api.test.FakeHeaders
import play.api.test.FakeApplication
import play.api.libs.iteratee.Enumerator
import org.specs2.specification.BeforeExample

class KeepReferrerFilterSpec extends PlaySpecification with Mockito with BeforeExample {

  var resultRequest:SimpleResult = _
  var action : Action[AnyContent] = _

  def before = {
    resultRequest = spy(SimpleResult(header = ResponseHeader(200, Map()), body = mock[Enumerator[Array[Byte]]]))
    action = Action { request => resultRequest }
  }

  "filter" should {
    "set correct referrer to session" in {
      running(FakeApplication(additionalConfiguration = Map("application.secret" -> "test"))) {
        val requestUrl = "/articles/new"
        val request = FakeRequest("GET", requestUrl, headers = FakeHeaders(), body = "")

        await(KeepReferrerFilter(action)(request).run)

        there was one(resultRequest).withSession(HeaderNames.REFERER -> requestUrl)
      }
    }

    "not set referrer for javascript resources" in {
      running(FakeApplication(additionalConfiguration = Map("application.secret" -> "test"))) {
        val requestUrl = "/javascripts/default.js"
        val request = FakeRequest("GET", requestUrl, headers = FakeHeaders(), body = "")

        await(KeepReferrerFilter(action)(request).run)

        there was no(resultRequest).withSession(HeaderNames.REFERER -> requestUrl)
      }
    }

    "not set referrer for style resources" in {
      running(FakeApplication(additionalConfiguration = Map("application.secret" -> "test"))) {
        val requestUrl = "/stylesheets/style.css"
        val request = FakeRequest("GET", requestUrl, headers = FakeHeaders(), body = "")

        await(KeepReferrerFilter(action)(request).run)

        there was no(resultRequest).withSession(HeaderNames.REFERER -> requestUrl)
      }
    }

    "not set referrer for images" in {
      running(FakeApplication(additionalConfiguration = Map("application.secret" -> "test"))) {
        val requestUrl = "/images/logo.png"
        val request = FakeRequest("GET", requestUrl, headers = FakeHeaders(), body = "")

        await(KeepReferrerFilter(action)(request).run)

        there was no(resultRequest).withSession(HeaderNames.REFERER -> requestUrl)
      }
    }

    "not set referrer for fonts" in {
      running(FakeApplication(additionalConfiguration = Map("application.secret" -> "test"))) {
        val requestUrl = "/fonts/FontAwesome.otf"
        val request = FakeRequest("GET", requestUrl, headers = FakeHeaders(), body = "")

        await(KeepReferrerFilter(action)(request).run)

        there was no(resultRequest).withSession(HeaderNames.REFERER -> requestUrl)
      }
    }

    "not set referrer for post requests" in {
      running(FakeApplication(additionalConfiguration = Map("application.secret" -> "test"))) {
        val requestUrl = "/articles/new"
        val request = FakeRequest("POST", requestUrl, headers = FakeHeaders(), body = "")

        await(KeepReferrerFilter(action)(request).run)

        there was no(resultRequest).withSession(HeaderNames.REFERER -> requestUrl)
      }
    }

    "not set referrer for ajax requests" in {
      running(FakeApplication(additionalConfiguration = Map("application.secret" -> "test"))) {
        val requestUrl = "/articles/new"
        val request = FakeRequest("GET", requestUrl,
          headers = FakeHeaders(Seq("X-Requested-With" -> Seq("XMLHttpRequest"))), body = "")

        await(KeepReferrerFilter(action)(request).run)

        there was no(resultRequest).withSession(HeaderNames.REFERER -> requestUrl)
      }
    }
  }
}