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

  val ignoredPaths = Seq(
    "/stylesheets",
    "/images",
    "/javascripts"
  )

  override def apply(next: (RequestHeader) => Future[SimpleResult])(rh: RequestHeader): Future[SimpleResult] = {
    next(rh).map { result =>
      rh.method match {
          case "GET" if !"XMLHttpRequest".equalsIgnoreCase(rh.headers.get("X-Requested-With").getOrElse(""))
            && !ignoredPaths.exists(x => rh.path.startsWith(x)) =>
            result.withSession(HeaderNames.REFERER -> rh.path)
          case _ => result
        }
    }
  }
}
