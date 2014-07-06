package security

import scala.language.implicitConversions
import models.database.UserRecord

/**
 * User abstraction for use in authentication managers. An authentication manager's knowledge
 * about user account is limited to auth credentials, so additional user info may be omitted.
 */
private[security] case class UserInfo(username: String, password: String, firstName: Option[String], lastName: Option[String])

private[security] object UserInfoImplicitConversions {
  implicit def UserRecordOption2UserInfoOption(record: UserRecord) =
    new UserInfo(record.username, record.password, record.firstName, record.lastName)
}




