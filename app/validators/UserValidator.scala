package validators

import models.UserModels.User

import scalaz._
import Scalaz._

object UserValidator {
  val USERNAME_MAX_LENGTH = 25
  val PASSWORD_MAX_LENGTH = 50
  val EMAIL_MAX_LENGTH = 50
  val EMAIL_FORMAT = "^[a-zA-Z0-9_'+*/^&=?~{}\\-](\\.?[a-zA-Z0-9_'+*/^&=?~{}\\-])" +
  "*\\@((\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}(\\:\\d{1,3})?)|(((([a-zA-Z0-9][a-zA-Z0-9\\-]" +
  "+[a-zA-Z0-9])|([a-zA-Z0-9]{1,2}))[\\.]{1})+([a-zA-Z]{2,6})))$"
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
