package validators

import models.UserModels.User

import scalaz._
import Scalaz._

object UserValidator {
  val USERNAME_MAX_LENGTH = 25
  val PASSWORD_MAX_LENGTH = 50
  val EMAIL_MAX_LENGTH = 50
  val EMAIL_FORMAT = """(?i)\b[A-Z0-9._%+-]+@[A-Z0-9.-]+\.[A-Z]{2,4}\b"""
}

class UserValidator extends Validator[User] {
  import UserValidator._

  override def validate(user: User): ValidationNel[String, User] = {

    def checkUsername = {
      if (user.username.trim.length > USERNAME_MAX_LENGTH || user.username.trim.isEmpty)
        s"Username length must be between 1 and $USERNAME_MAX_LENGTH characters".failNel
      else
        user.successNel
    }

    def checkPassword = {
      if (user.password.trim.length > PASSWORD_MAX_LENGTH  || user.password.trim.isEmpty)
        s"Password length must be between 1 and $PASSWORD_MAX_LENGTH characters".failNel
      else
        user.successNel
    }

    def checkEmail = {
      if (user.email.trim.isEmpty || !user.email.matches(EMAIL_FORMAT))
        "An email format should be like mail@mail.ru".failNel
      else if (user.email.trim.length >= EMAIL_MAX_LENGTH)
        s"Email field should not contain more than $EMAIL_MAX_LENGTH symbols.".failNel
      else
        user.successNel
    }
    (checkEmail |@| checkUsername |@| checkPassword) {
      case _ => user
    }

  }
}
