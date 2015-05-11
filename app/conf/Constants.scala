package conf

import org.joda.time.DateTimeConstants

object Constants {

  val PAGE_SIZE_ARTICLES = 10
  val PAGE_SIZE_USERS = 50
  val rememberMeCookie = "remember_token"
  //org.jboss.netty.handler.codec.http.cookie.ServerCookieEncoder.encode
  //see at line => Date expires = new Date(cookie.maxAge() * 1000 + System.currentTimeMillis());
  //multiply by 1000 can produce integer > Integer.MAX_VALUE if rememberMeExpirationTime > 24 days
  val rememberMeExpirationTime = DateTimeConstants.SECONDS_PER_DAY * 24
}
