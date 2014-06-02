package controllers.filters

import play.api.mvc._
import scala.concurrent.Future
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.mvc.Http.HeaderNames

/**
 *
 */
object KeepReferrerFilter extends Filter {
  override def apply(next: (RequestHeader) => Future[SimpleResult])(rh: RequestHeader): Future[SimpleResult] = {
    next(rh).map { result =>
      result.withSession(HeaderNames.REFERER -> rh.path)
    }
  }
}
