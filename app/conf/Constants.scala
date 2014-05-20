package conf

import org.joda.time.DateTimeConstants

object Constants {

  val PAGE_SIZE_ARTICLES = 10
  val PAGE_SIZE_USERS = 50
  val rememberMeCookie = "remember_token"
  val rememberMeExpirationTime = DateTimeConstants.SECONDS_PER_WEEK * 4
}
