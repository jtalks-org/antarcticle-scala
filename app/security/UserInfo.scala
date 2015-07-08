package security

import scala.language.implicitConversions
import models.database.UserRecord

/**
 * User abstraction for use in authentication managers. An authentication manager's knowledge
 * about user account is limited to auth credentials, so additional user info may be omitted.
 */
private[security] case class UserInfo(username: String, password: String, email: String,
                                      firstName: Option[String] = None, lastName: Option[String] = None,
                                      active: Boolean = false)

private[security] object UserInfoImplicitConversions {
  implicit def UserRecordOption2UserInfoOption(record: UserRecord): UserInfo =
    new UserInfo(record.username, record.password, record.email, record.firstName, record.lastName, record.active)
}




