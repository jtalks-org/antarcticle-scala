package conf

import org.joda.time.DateTimeConstants

object Constants {

  val PAGE_SIZE = 10
  val rememberMeCookie = "remember_token"
  val rememberMeExpirationTime = DateTimeConstants.SECONDS_PER_WEEK * 4
}
