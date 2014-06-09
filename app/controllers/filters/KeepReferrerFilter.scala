package controllers.filters

import play.api.mvc._
import scala.concurrent.Future
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.mvc.Http.HeaderNames

/**
 * Saves request path in session. We need it for case when user requests page directly (not by link).
 * Ignores requests for static resources as well as POST and all ajax requests.
 */
object KeepReferrerFilter extends Filter {

  // todo: it would be nice to extract path prefixes from router or controllers.Assets instead of hardcoding 'em here
  val ignoredPaths = Seq(
    "/fonts/",
    "/stylesheets/",
    "/images/",
    "/javascripts/"
  )

  override def apply(next: (RequestHeader) => Future[SimpleResult])(rh: RequestHeader): Future[SimpleResult] = {

    def shouldSaveReferrer(rh: RequestHeader) = rh.method == "GET" &&
      !"XMLHttpRequest".equalsIgnoreCase(rh.headers.get("X-Requested-With").getOrElse("")) &&
      !ignoredPaths.exists(x => rh.path.contains(x))

    next(rh).map {
      result => if (shouldSaveReferrer(rh)) result.withSession(HeaderNames.REFERER -> rh.path) else result
    }
  }
}
