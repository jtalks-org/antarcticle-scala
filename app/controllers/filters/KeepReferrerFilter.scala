package controllers.filters

import play.api.mvc._
import scala.concurrent.Future
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.mvc.Http.HeaderNames

/**
 *
 */
object KeepReferrerFilter extends Filter {

  val appliedRoutes = Seq(
    "/articles",
    "/users",
    "/roles",
    "/help/markdown"
  )

  override def apply(next: (RequestHeader) => Future[SimpleResult])(rh: RequestHeader): Future[SimpleResult] = {
    next(rh).map { result =>
      rh.method match {
          case "GET" if appliedRoutes.exists(x => rh.path.startsWith(x)) =>
                                      result.withSession(HeaderNames.REFERER -> rh.path)
          case _ => result
        }
    }
  }
}
